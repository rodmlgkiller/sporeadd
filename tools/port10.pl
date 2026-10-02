#!/usr/bin/perl
# Phase 10: hurtAndBreak, block props, codecs, finalizeSpawn, misc renames.
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

  # hurtAndBreak(n, entity, lambda calling broadcastBreakEvent(x))
  $src =~ s{(\.hurtAndBreak\s*\()($bal)\)}{
      my ($pre, $args) = ($1, $2);
      my @a = split_args($args);
      if (@a == 3 && $a[2] =~ /->/ && $a[2] =~ /broadcastBreakEvent\(\s*($bal)\)/) {
        my $x = $1; $x =~ s/^\s+|\s+$//g;
        my $slot = $x =~ /^(?:net\.minecraft\.world\.entity\.)?EquipmentSlot\./ ? $x : "net.minecraft.world.entity.LivingEntity.getSlotForHand($x)";
        "$pre$a[0],$a[1], $slot)";
      } else { "$pre$args)" }
  }ge;

  # block properties copy
  $src =~ s/\b((?:BlockBehaviour\.)?Properties)\.copy\(/BlockBehaviour.Properties.ofFullCopy(/g;

  # Mob#finalizeSpawn: drop trailing CompoundTag argument (5 -> 4 args) on call sites
  $src =~ s{(\.finalizeSpawn\s*\()($bal)\)}{
      my ($pre, $args) = ($1, $2);
      my @a = split_args($args);
      (@a == 5) ? "$pre" . join(',', @a[0..3]) . ")" : "$pre$args)";
  }ge;

  # LivingEntity#dropCustomDeathLoot(DamageSource, int, boolean) -> (ServerLevel, DamageSource, boolean)
  $src = xform($src,
    qr/(?<pre>\bprotected\s+void\s+dropCustomDeathLoot\s*)\(\s*(?:net\.minecraft\.world\.damagesource\.)?DamageSource\s+(?<d>\w+)\s*,\s*int\s+(?<l>\w+)\s*,\s*boolean\s+(?<r>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my ($d, $l, $r) = @$c{qw(d l r)};
      $body =~ s/super\.dropCustomDeathLoot\(\s*\Q$d\E\s*,\s*\Q$l\E\s*,\s*\Q$r\E\s*\)/super.dropCustomDeathLoot(serverLevel, $d, $r)/g;
      return ("$c->{pre}(net.minecraft.server.level.ServerLevel serverLevel, net.minecraft.world.damagesource.DamageSource $d, boolean $r) ", $body);
    });

  # misc renames
  $src =~ s/\bBlocks\.GRASS\b/Blocks.SHORT_GRASS/g;
  $src =~ s/\b(SoundEvents\.\w+)\.get\(\)/$1/g;
  $src =~ s/\bBuiltInRegistries\.(\w+)\.getRegistryKey\(\)/BuiltInRegistries.$1.key()/g;
  $src =~ s/\b(\w+)\.hasCustomHoverName\(\)/$1.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)/g;
  $src =~ s/\b(\w+)\.setHoverName\(($bal)\)/$1.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, $2)/g;
  $src =~ s/^import PlayerTickEvent\.Post;\r?\n//mg;

  # BaseEntityBlock needs a codec
  if ($src =~ /public\s+class\s+(\w+)\s+extends\s+BaseEntityBlock\s*\{/ && $src !~ /MapCodec/) {
    my $cn = $1;
    $src =~ s/(public\s+class\s+\Q$cn\E\s+extends\s+BaseEntityBlock\s*\{)/$1\n\n    public static final com.mojang.serialization.MapCodec<$cn> CODEC = simpleCodec($cn\::new);\n\n    \@Override\n    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {\n        return CODEC;\n    }/;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
