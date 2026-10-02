#!/usr/bin/perl
# Phase 1d: point network/dist imports at the in-mod compatibility shims.
use strict; use warnings;
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh; my $orig = $src;
  $src =~ s/net\.neoforged\.neoforge\.network\.NetworkEvent/com.sporeadds.sporeaddsmod.network.NetworkEvent/g;
  $src =~ s/net\.neoforged\.neoforge\.network\.NetworkDirection/com.sporeadds.sporeaddsmod.network.NetworkDirection/g;
  $src =~ s/net\.neoforged\.neoforge\.network\.PacketDistributor/com.sporeadds.sporeaddsmod.network.PacketDistributor/g;
  $src =~ s/net\.neoforged\.neoforge\.network\.NetworkHooks/com.sporeadds.sporeaddsmod.network.NetworkHooks/g;
  $src =~ s/net\.neoforged\.fml\.DistExecutor/com.sporeadds.sporeaddsmod.util.DistExecutor/g;
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
