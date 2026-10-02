#!/usr/bin/perl
# Phase 8b: clean-ups after port8
use strict; use warnings;
my $bal = qr/(?:[^()]|\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))*/;
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  my ($class) = $f =~ m{([^/]+)[.]java$};
  $class = lc($class // 'x');

  $src =~ s/"(\w+_)"\.lc\("(\w+)"\)/'"' . $1 . lc($2) . '"'/ge;
  $src =~ s/^\s*cures\.remove\(net\.neoforged\.neoforge\.common\.EffectCures\.HONEY_BOTTLE\);\r?\n//mg;

  # holders for "this" inside MobEffect subclasses
  if ($src =~ /extends\s+MobEffect\b/) {
    my $h = 'net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this)';
    $src =~ s/\b(removeEffect|hasEffect|getEffect)\(this\)/$1($h)/g;
    $src =~ s/(new\s+(?:net\.minecraft\.world\.effect\.)?MobEffectInstance\(\s*)this\b/$1$h/g;
  }

  # addAttributeModifier(attr, "uuid-string"|ID.toString(), amount, op) -> ResourceLocation id
  my $n = 0;
  $src =~ s{(\baddAttributeModifier\s*\()($bal)\)}{
      my ($pre, $args) = ($1, $2);
      my @a = split /,(?![^()]*\))/, $args, 4;
      if (@a == 4) {
        if ($a[1] =~ /^\s*"/) { $n++; $a[1] = " ResourceLocation.fromNamespaceAndPath(\"sporeadd\", \"${class}_mod$n\")"; }
        elsif ($a[1] =~ /^(\s*)(\w+)\.toString\(\)\s*$/) { $a[1] = "$1$2"; }
        $pre . join(',', @a) . ')';
      } else { "$pre$args)" }
  }ge;

  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
