#!/usr/bin/perl
# Phase 9: LivingTickEvent -> EntityTickEvent.Pre, leftovers.
use strict; use warnings;
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

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  $src =~ s/EventBusSubscriber\.Bus\.FORGE/EventBusSubscriber.Bus.GAME/g;
  $src =~ s/\.connection\.connection\b/.connection.getConnection()/g;

  if ($src =~ /LivingEvent\.LivingTickEvent/) {
    $src = xform($src,
      qr/(?<pre>\bpublic\s+static\s+void\s+\w+\s*)\(\s*LivingEvent\.LivingTickEvent\s+(?<ev>\w+)\s*\)\s*\{/,
      sub {
        my ($sig, $body, $c) = @_;
        my $ev = $c->{ev};
        $body =~ s/^(\s*)LivingEntity\s+(\w+)\s*=\s*\Q$ev\E\.getEntity\(\);/$1if (!($ev.getEntity() instanceof LivingEntity $2)) return;/m;
        return ("$c->{pre}(EntityTickEvent.Pre $ev) ", $body);
      });
    $src =~ s/LivingEvent\.LivingTickEvent/EntityTickEvent.Pre/g;
    if ($src !~ /^import net\.neoforged\.neoforge\.event\.tick\.EntityTickEvent;/m) {
      $src =~ s/^(package [^;]+;\r?\n)/$1\nimport net.neoforged.neoforge.event.tick.EntityTickEvent;\n/m;
    }
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
