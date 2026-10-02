use strict; use warnings;
# errs.pl LOG [FILTER]: print "rel:line: message | source" for each javac error
my ($log, $filter) = @ARGV;
open(my $fh, '<', $log) or die;
my %cache;
my @out;
while (my $l = <$fh>) {
  next unless $l =~ m{^(.*?[.]java):(\d+): error: (.*)$};
  my ($f, $n, $m) = ($1, $2, $3);
  my $p = join('/', split(/[\x5c]/, $f));
  my $rel = $p;
  $rel =~ s{^.*?/sporeadds/}{};
  next if defined $filter && $rel !~ /$filter/ && $m !~ /$filter/;
  if (!exists $cache{$p}) {
    my @lines;
    if (open(my $s, '<:encoding(UTF-8)', $p)) { @lines = <$s>; close $s; }
    $cache{$p} = \@lines;
  }
  my $src = $cache{$p}[$n - 1] // '';
  $src =~ s/^\s+//;
  $src =~ s/\s+$//;
  push @out, "$rel:$n: $m | $src";
}
print "$_\n" for @out;
