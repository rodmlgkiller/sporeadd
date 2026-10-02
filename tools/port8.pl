#!/usr/bin/perl
# Phase 8: attribute modifiers (ResourceLocation ids), ForgeMod attributes, MobEffect curative items.
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

my $bal = qr/(?:[^()]|\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))*/;

sub split_args {
  my ($s) = @_;
  my @a; my $depth = 0; my $cur = ''; my $instr = 0;
  for my $c (split //, $s) {
    if ($c eq '"') { $instr = !$instr; }
    if (!$instr) {
      if ($c eq '(') { $depth++; } elsif ($c eq ')') { $depth--; }
      elsif ($c eq ',' && $depth == 0) { push @a, $cur; $cur = ''; next; }
    }
    $cur .= $c;
  }
  push @a, $cur;
  return @a;
}

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  my ($class) = $f =~ m{([^\/]+).java$};
  $class = lc($class // 'x');

  # UUID modifier ids -> ResourceLocation ids
  $src =~ s/(\bprivate\s+static\s+final\s+)UUID(\s+)(\w+)(\s*=\s*)UUID\.fromString\("[^"]*"\)/$1ResourceLocation$2$3$4ResourceLocation.fromNamespaceAndPath("sporeadd", "${class}_".lc("$3"))/g;

  # new AttributeModifier(id, "name", amount, op) -> new AttributeModifier(id, amount, op)
  $src =~ s{new\s+((?:net\.minecraft\.world\.entity\.ai\.attributes\.)?AttributeModifier)\s*\(($bal)\)}{
      my ($cls, $args) = ($1, $2);
      my @a = split_args($args);
      if (@a == 4 && $a[1] =~ /^\s*"/ ) { "new $cls($a[0],$a[2],$a[3])" }
      elsif (@a == 4) { "new $cls($a[0],$a[2],$a[3])" }
      else { "new $cls($args)" }
  }ge;

  $src =~ s/\bOperation\.ADDITION\b/Operation.ADD_VALUE/g;
  $src =~ s/\bOperation\.MULTIPLY_BASE\b/Operation.ADD_MULTIPLIED_BASE/g;
  $src =~ s/\bOperation\.MULTIPLY_TOTAL\b/Operation.ADD_MULTIPLIED_TOTAL/g;

  # ForgeMod -> NeoForgeMod / vanilla attributes
  $src =~ s/\b(?:net\.neoforged\.neoforge\.common\.)?ForgeMod\.STEP_HEIGHT_ADDITION\.get\(\)/net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT/g;
  $src =~ s/\b(?:net\.neoforged\.neoforge\.common\.)?ForgeMod\.SWIM_SPEED\.get\(\)/net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED/g;
  $src =~ s/\b(?:net\.neoforged\.neoforge\.common\.)?ForgeMod\.WATER_TYPE\.get\(\)/net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value()/g;
  $src =~ s/^import net\.neoforged\.neoforge\.common\.ForgeMod;\r?\n//mg;

  # MobEffect attribute hooks
  $src = xform($src,
    qr/(?<pre>\bpublic\s+void\s+addAttributeModifiers\s*)\(\s*(?:[\w.]+\.)?LivingEntity\s+(?<e>\w+)\s*,\s*(?:[\w.]+\.)?AttributeMap\s+(?<m>\w+)\s*,\s*int\s+(?<a>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my ($e, $m, $a) = @$c{qw(e m a)};
      $body =~ s/super\.addAttributeModifiers\(\s*\Q$e\E\s*,\s*\Q$m\E\s*,\s*\Q$a\E\s*\)/super.addAttributeModifiers($m, $a)/g;
      $body =~ s/\bremoveAttributeModifiers\(\s*\Q$e\E\s*,\s*\Q$m\E\s*,\s*\Q$a\E\s*\)/removeAttributeModifiers($m)/g;
      return ("$c->{pre}(net.minecraft.world.entity.ai.attributes.AttributeMap $m, int $a) ", $body);
    });
  $src = xform($src,
    qr/(?<pre>\bpublic\s+void\s+removeAttributeModifiers\s*)\(\s*(?:[\w.]+\.)?LivingEntity\s+(?<e>\w+)\s*,\s*(?:[\w.]+\.)?AttributeMap\s+(?<m>\w+)\s*,\s*int\s+(?<a>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my ($e, $m, $a) = @$c{qw(e m a)};
      $body =~ s/super\.removeAttributeModifiers\(\s*\Q$e\E\s*,\s*\Q$m\E\s*,\s*\Q$a\E\s*\)/super.removeAttributeModifiers($m)/g;
      return ("$c->{pre}(net.minecraft.world.entity.ai.attributes.AttributeMap $m) ", $body);
    });

  # getCurativeItems -> fillEffectCures (default cures are kept unless the old list was empty)
  $src = xform($src,
    qr/(?:\@Override\s+)?\bpublic\s+(?:java\.util\.)?List<(?:net\.minecraft\.world\.item\.)?ItemStack>\s+getCurativeItems\s*\(\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      return ("\@Override\n    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) ",
              "\n        cures.remove(net.neoforged.neoforge.common.EffectCures.MILK);\n        cures.remove(net.neoforged.neoforge.common.EffectCures.HONEY_BOTTLE);\n    ")
        unless $body =~ /MILK_BUCKET/;
      return ("\@Override\n    public void fillEffectCures(java.util.Set<net.neoforged.neoforge.common.EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) ", "\n    ");
    });
  $src =~ s/\@Override\s*\n(\s*)\@Override\s*\n/\@Override\n/g;

  if ($src =~ /\bResourceLocation\b/ && $src !~ /^import net\.minecraft\.resources\.ResourceLocation;/m) {
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport net.minecraft.resources.ResourceLocation;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
