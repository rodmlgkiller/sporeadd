#!/usr/bin/perl
# Phase 7: item NBT -> ItemNbt shim (CUSTOM_DATA component)
use strict; use warnings;
my $call = qr/[A-Za-z_]\w*(?:\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))?/;
my $recv = qr/(?:$call\.)*$call/;
my $bal  = qr/(?:[^()]|\((?:[^()]|\([^()]*\))*\))*/;

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  next if $f =~ /ItemNbt\.java$/;
  next unless $src =~ /\.(?:getOrCreateTag|getTag|hasTag|setTag)\(/;

  $src =~ s/($recv)\.getOrCreateTag\(\)/ItemNbt.getOrCreateTag($1)/g;
  $src =~ s/($recv)\.getTag\(\)/ItemNbt.getTag($1)/g;
  $src =~ s/($recv)\.hasTag\(\)/ItemNbt.hasTag($1)/g;
  $src =~ s/($recv)\.setTag\(($bal)\)/ItemNbt.setTag($1, $2)/g;

  if (index($src, 'ItemNbt.') >= 0 && index($src, 'util.ItemNbt;') < 0) {
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport com.sporeadds.sporeaddsmod.util.ItemNbt;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
