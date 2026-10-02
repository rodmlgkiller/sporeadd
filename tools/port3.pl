#!/usr/bin/perl
# Phase 3: capabilities -> data attachments. RECV.getCapability(Foo.CONST) -> Foo.CONST.get(RECV)
use strict; use warnings;
my $call = qr/[A-Za-z_]\w*(?:\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))?/;
my $recv = qr/(?:$call\.)*$call/;

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  $src =~ s{($recv)\.getCapability\(\s*((?:\w+\.)+[A-Z][A-Z_0-9]*)\s*\)}{$2.get($1)}g;
  $src =~ s/net\.neoforged\.neoforge\.common\.capabilities\.Capability\b/com.sporeadds.sporeaddsmod.capabilities.Capability/g;
  $src =~ s/net\.neoforged\.neoforge\.common\.util\.LazyOptional\b/com.sporeadds.sporeaddsmod.capabilities.LazyOptional/g;

  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
