#!/usr/bin/perl
# Phase 12: remaining MobEffect/Holder fixes, MobEffectEvent.Applicable result.
use strict; use warnings;
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  my $needHolder = 0;

  $needHolder = 1 if $src =~ s/\bMobEffect(\s+\w+\s*=\s*(?:effects|Seffects)\.[A-Z_0-9]+)\.get\(\);/Holder<MobEffect>$1;/g;
  $src =~ s/\bvar(\s+\w+\s*=\s*(?:effects|Seffects)\.[A-Z_0-9]+)\.get\(\);/var$1;/g;
  $src =~ s/\(MobEffect\)\s*(Seffects\.[A-Z_0-9]+)\.get\(\)/$1/g;
  $src =~ s/(MOB_EFFECT\.getKey\(\s*)(MobEffects\.\w+|[\w.]+\.getEffect\(\)|incomingEffect)(\s*\))/$1$2.value()$3/g;
  $needHolder = 1 if $src =~ s/\bMobEffect(\s+incomingEffect\s*=\s*event\.getEffectInstance\(\)\.getEffect\(\))/Holder<MobEffect>$1/g;
  $needHolder = 1 if $src =~ s/\bstatic MobEffect getDissolutionEffect\(\)/static Holder<MobEffect> getDissolutionEffect()/g;
  $needHolder = 1 if $src =~ s/\bMobEffect(\s+dissolution\s*=\s*getDissolutionEffect\(\))/Holder<MobEffect>$1/g;

  if ($src =~ /MobEffectEvent\.Applicable/) {
    $src =~ s/\bEvent\.Result\.DENY\b/MobEffectEvent.Applicable.Result.DO_NOT_APPLY/g;
    $src =~ s/\bEvent\.Result\.ALLOW\b/MobEffectEvent.Applicable.Result.APPLY/g;
    $src =~ s/\bEvent\.Result\.DEFAULT\b/MobEffectEvent.Applicable.Result.DEFAULT/g;
  }
  if ($needHolder && $src !~ /^import net\.minecraft\.core\.Holder;/m) {
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport net.minecraft.core.Holder;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
