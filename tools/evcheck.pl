#!/usr/bin/perl
# evcheck.pl: verify every @SubscribeEvent method listens to a concrete, existing event class (abstract bases throw at load time)
use strict; use warnings;
my %seen; my %where;
my @files = split /\n/, `find src/main/java -name "*.java"`;
for my $f (@files) {
  open(my $fh, '<:encoding(UTF-8)', $f) or next; local $/; my $src = <$fh>; close $fh;
  my %imp;
  while ($src =~ /^\s*import\s+([\w.\$]+)\.(\w+);/mg) { $imp{$2} = "$1.$2"; }
  while ($src =~ /\@SubscribeEvent(?:\([^)]*\))?\s*(?:\r?\n\s*)?(?:public\s+)?(?:static\s+)?void\s+(\w+)\s*\(\s*(?:final\s+)?([\w.]+)\s+\w+\s*\)/g) {
    my ($m, $t) = ($1, $2);
    my $fq;
    if ($t =~ /^([A-Za-z]\w*)\.(.+)$/ && $imp{$1}) { $fq = $imp{$1} . '$' . join('$', split /\./, $2); }
    elsif ($imp{$t}) { $fq = $imp{$t}; }
    elsif ($t =~ /[.]/) { my @p = split /\./, $t; $fq = join('.', @p[0..$#p-1]) . '$' . $p[-1]; }
    else { $fq = $t; }
    $seen{$fq} = 1; push @{ $where{$fq} }, "$f::$m";
  }
}
for my $fq (sort keys %seen) {
  next unless $fq =~ /^(net|org)\./;
  my $out = `bash tools/jp.sh '$fq' 2>&1 | head -3`;
  if ($out !~ /class|interface/ || $out =~ /not found/) { print "NOT FOUND: $fq  (", join(', ', @{ $where{$fq} }), ")\n"; next; }
  if ($out =~ /\babstract class\b/) { print "ABSTRACT: $fq  (", join(', ', @{ $where{$fq} }), ")\n"; }
}
print "checked ", scalar(keys %seen), " event types\n";
