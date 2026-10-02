use strict; use warnings;
# Tally the method signatures that follow "method does not override" errors in a javac log.
my %c;
open(my $log, '<', $ARGV[0]) or die;
while (my $ln = <$log>) {
  next unless $ln =~ m{^(.*\.java):(\d+): error: method does not override};
  my ($file, $num) = ($1, $2);
  $file = join('/', split(/\\/, $file));
  open(my $src, '<:encoding(UTF-8)', $file) or next;
  my @lines = <$src>;
  close $src;
  my $text = $lines[$num] // '';
  $text =~ s/^\s+//;
  $text =~ s/\{.*//s;
  $text =~ s/\s+$//;
  $c{$text}++;
}
print "$c{$_} $_\n" for sort { $c{$b} <=> $c{$a} } keys %c;
