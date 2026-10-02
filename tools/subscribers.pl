#!/usr/bin/perl
# subscribers.pl [--fix]: report (or strip) @EventBusSubscriber on classes that have no @SubscribeEvent method of their own
use strict; use warnings;
my $fix = grep { $_ eq '--fix' } @ARGV;
my @files = grep { $_ ne '--fix' } @ARGV;
sub find_close {
  my ($s, $open) = @_;
  my ($d, $n, $i) = (0, length $s, $open);
  while ($i < $n) {
    my $c = substr($s, $i, 1);
    if ($c eq '"') {
      if (substr($s, $i, 3) eq '"""') { my $e = index($s, '"""', $i + 3); return -1 if $e < 0; $i = $e + 3; next; }
      $i++;
      while ($i < $n) { my $x = substr($s, $i, 1); if ($x eq '\\') { $i += 2; next; } last if $x eq '"'; $i++; }
      $i++; next;
    } elsif ($c eq "'") {
      $i++;
      while ($i < $n) { my $x = substr($s, $i, 1); if ($x eq '\\') { $i += 2; next; } last if $x eq "'"; $i++; }
      $i++; next;
    } elsif ($c eq '/' && substr($s, $i + 1, 1) eq '/') {
      $i = index($s, "\n", $i); $i = $n if $i < 0; next;
    } elsif ($c eq '/' && substr($s, $i + 1, 1) eq '*') {
      my $e = index($s, '*/', $i); return -1 if $e < 0; $i = $e + 2; next;
    } elsif ($c eq '{') { $d++; }
    elsif ($c eq '}') { $d--; return $i if $d == 0; }
    $i++;
  }
  return -1;
}

# Apply $cb->($sigtext_without_brace, $body, \%captures) to every method whose header matches $re (which must end with '{').
sub xform {
  my ($src, $re, $cb) = @_;
  my @m;
  while ($src =~ /$re/g) { push @m, [$-[0], $+[0], {%+}]; }
  for my $m (reverse @m) {
    my ($s, $e, $cap) = @$m;
    my $open = $e - 1;
    my $close = find_close($src, $open);
    next if $close < 0;
    my $body = substr($src, $open + 1, $close - $open - 1);
    my $sig  = substr($src, $s, $e - $s - 1);
    my ($nsig, $nbody) = $cb->($sig, $body, $cap);
    substr($src, $s, $close + 1 - $s) = $nsig . '{' . $nbody . '}';
  }
  return $src;
}

for my $f (@files) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  my @hits;
  while ($src =~ /(\@EventBusSubscriber(?:\([^)]*\))?)(\s*\r?\n\s*)((?:public|static|final|abstract|\s)*class\s+\w+[^{]*)\{/g) {
    push @hits, [$-[1], $+[0] - 1, length($1) + length($2)];
  }
  for my $h (reverse @hits) {
    my ($start, $open, $annlen) = @$h;
    my $close = find_close($src, $open);
    next if $close < 0;
    my $body = substr($src, $open + 1, $close - $open - 1);
    # drop nested class bodies
    my $guard = 0;
    while ($body =~ /\b(?:class|interface|enum|record)\s+\w+[^{;]*\{/g && $guard++ < 50) {
      my $o = $+[0] - 1;
      my $c = find_close($body, $o);
      last if $c < 0;
      substr($body, $o, $c - $o + 1) = '{}';
      pos($body) = 0;
    }
    if ($body !~ /\@SubscribeEvent/) {
      print "no subscribers: $f (offset $start)\n";
      substr($src, $start, $annlen) = '' if $fix;
    }
  }
  if ($fix && $src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
