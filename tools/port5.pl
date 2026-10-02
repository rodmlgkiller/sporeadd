#!/usr/bin/perl
# Phase 5: MobEffect -> Holder<MobEffect>
use strict; use warnings;
my $bal = qr/(?:[^()]|\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))*/;
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  # registry lookups that yield an effect become Holder lookups
  $src =~ s/BuiltInRegistries\.MOB_EFFECT(\s*)\.get\(($bal)\)/BuiltInRegistries.MOB_EFFECT$1.getHolder($2).orElse(null)/g;
  my $needHolder = 0;
  $needHolder = 1 if $src =~ s/\b(?:net\.minecraft\.world\.effect\.)?MobEffect(\s+\w+\s*=\s*BuiltInRegistries\.MOB_EFFECT)/Holder<MobEffect>$1/g;

  # DeferredHolder.get() -> the holder itself where a Holder<MobEffect> is expected
  my $eff = qr/(?:\w+\.)*(?:effects|Seffects)\.[A-Z][A-Z_0-9]*/;
  $src =~ s/\b(removeEffect|hasEffect|getEffect)\(($eff)\.get\(\)/$1($2/g;
  $src =~ s/(\bMobEffectInstance\(\s*)($eff)\.get\(\)/$1$2/g;

  if ($needHolder && $src !~ /^import net\.minecraft\.core\.Holder;/m) {
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport net.minecraft.core.Holder;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
