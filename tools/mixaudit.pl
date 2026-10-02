#!/usr/bin/perl
# mixaudit.pl: static check of every mixin's target class and injected method names against the real class files.
# usage: perl tools/mixaudit.pl [MixinFileNameFilter]
use strict; use warnings;

my $dir = 'src/main/java/com/sporeadds/mixin';
my $filter = shift // '';
my %members;   # fq class -> { name => [descriptors] }  (undef when the class is missing)

sub load_class {
  my ($fq) = @_;
  return $members{$fq} if exists $members{$fq};
  my $out = `bash tools/jp.sh '$fq' -p -s 2>&1`;
  if ($out =~ /Error: class not found|not found/ && $out !~ /Compiled from/) { return $members{$fq} = undef; }
  my %m;
  my @lines = split /\n/, $out;
  my $short = $fq; $short =~ s/.*[.]//; $short =~ s/.*[\$]//;
  for (my $i = 0; $i < @lines; $i++) {
    my $l = $lines[$i];
    next unless $l =~ /^\s{2}\S/;
    my $d = ($lines[$i + 1] // '');
    next unless $d =~ /descriptor:\s*(\S+)/;
    my $desc = $1;
    my $name;
    if ($l =~ /^\s{2}static \{\};/) { $name = '<clinit>'; }
    elsif ($l =~ /\b([\w\$]+)\(/) { $name = $1; }
    elsif ($l =~ /\b([\w\$]+);\s*$/) { $name = $1; }
    next unless defined $name;
    $name = '<init>' if $name eq $short || $l =~ /^\s{2}(?:public|protected|private)?\s*\Q$fq\E\(/;
    push @{ $m{$name} }, $desc;
  }
  return $members{$fq} = \%m;
}

opendir(my $dh, $dir) or die;
my @files = sort grep { /[.]java$/ && ($filter eq '' || /$filter/) } readdir $dh;
closedir $dh;

my ($bad, $total) = (0, 0);
for my $f (@files) {
  open(my $fh, '<:encoding(UTF-8)', "$dir/$f") or next;
  local $/; my $src = <$fh>; close $fh;

  my %imports;
  while ($src =~ /^import\s+([\w.\$]+)\.(\w+);/mg) { $imports{$2} = "$1.$2"; }

  my @targets;
  if ($src =~ /\@Mixin\s*\((.*?)\)\s*\r?\n\s*(?:public\s+)?(?:abstract\s+)?(?:class|interface)/s) {
    my $args = $1;
    while ($args =~ /([\w.\$]+)\.class/g) {
      my $n = $1;
      my $fq = $n =~ /[.]/ ? $n : ($imports{$n} // $n);
      push @targets, $fq;
    }
    while ($args =~ /targets\s*=\s*"([^"]+)"/g) { push @targets, $1; }
  }
  unless (@targets) { print "?? $f: no Mixin annotation target parsed\n"; next; }

  # nested class names written as Outer.Inner -> Outer$Inner
  for my $t (@targets) {
    if (!load_class($t) && $t =~ /^(.*)[.]([A-Z]\w*)$/) {
      my $alt = "$1\$$2";
      if (load_class($alt)) { $t = $alt; }
    }
  }

  my @methods;
  while ($src =~ /method\s*=\s*(\{[^}]*\}|"[^"]*")/g) {
    my $v = $1;
    push @methods, $v =~ /"([^"]*)"/g;
  }

  for my $t (@targets) {
    my $cls = load_class($t);
    $total++;
    if (!$cls) { print "MISSING CLASS  $f -> $t\n"; $bad++; next; }
    for my $m (@methods) {
      my ($name, $desc) = $m =~ /^([^(]+)(\(.*)?$/;
      $name =~ s/\s+//g;
      $total++;
      my $have = $cls->{$name};
      if (!$have) { print "MISSING METHOD $f -> $t#$name\n"; $bad++; next; }
      if (defined $desc && $desc ne '' && !grep { $_ eq $desc } @$have) {
        print "DESC MISMATCH  $f -> $t#$name$desc\n               have: @$have\n"; $bad++;
      }
    }
  }
}
print "checked $total targets/methods, $bad problems\n";
