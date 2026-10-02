#!/usr/bin/perl
# Phase 6: method-signature migrations (tooltips, effects, synched data, models, blocks, block entities, spawn).
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

sub add_import {
  my ($srcref, $fqn) = @_;
  return if $$srcref =~ /^import \Q$fqn\E;/m;
  $$srcref =~ s/^(package [^;]+;\r?\n)/$1\nimport $fqn;\n/m;
}

my $ann = qr/(?:\@[\w.]+(?:\([^)]*\))?\s+)*/;

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  # --- appendHoverText(ItemStack, Level, List, TooltipFlag) -> TooltipContext
  $src = xform($src,
    qr/(?<pre>\bpublic\s+void\s+appendHoverText\s*)\(\s*(?<st>(?:\@[\w.]+\s+)?(?:net\.minecraft\.world\.item\.)?ItemStack\s+(?<stack>\w+))\s*,\s*(?:\@[\w.]+\s+)?(?:net\.minecraft\.world\.level\.)?Level\s+(?<lvl>\w+)\s*,(?<rest>[^()]*)\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my ($stack, $lvl, $rest) = ($c->{stack}, $c->{lvl}, $c->{rest});
      $body =~ s/super\.appendHoverText\(\s*\Q$stack\E\s*,\s*\Q$lvl\E\s*,/super.appendHoverText($stack, context,/g;
      my $decl = "\n        net.minecraft.world.level.Level $lvl = context.level();";
      return ("$c->{pre}($c->{st}, net.minecraft.world.item.Item.TooltipContext context,$rest) ", $decl . $body);
    });

  # --- MobEffect hooks
  $src =~ s/\bisDurationEffectTick\b/shouldApplyEffectTickThisTick/g;
  $src = xform($src,
    qr/(?<pre>\bpublic\s+)void(?<mid>\s+applyEffectTick\s*\(\s*(?:net\.minecraft\.world\.entity\.)?LivingEntity\s+\w+\s*,\s*int\s+\w+\s*\))\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      $body =~ s/\breturn\s*;/return true;/g;
      my $trim = $body; $trim =~ s/\s+$//;
      $body .= "    return true;\n    " unless $trim =~ /return true;\z/;
      return ("$c->{pre}boolean$c->{mid} ", $body);
    });

  # --- SynchedEntityData builder
  $src = xform($src,
    qr/(?<pre>\bprotected\s+void\s+defineSynchedData\s*)\(\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      $body =~ s/super\.defineSynchedData\(\s*\)/super.defineSynchedData(builder)/g;
      $body =~ s/\b(?:this\.)?entityData\.define\(/builder.define(/g;
      return ("$c->{pre}(net.minecraft.network.syncher.SynchedEntityData.Builder builder) ", $body);
    });

  # --- Model#renderToBuffer colour packed into one int
  $src = xform($src,
    qr/(?<pre>\bpublic\s+void\s+renderToBuffer\s*)\(\s*(?:com\.mojang\.blaze3d\.vertex\.)?PoseStack\s+(?<ps>\w+)\s*,\s*(?:com\.mojang\.blaze3d\.vertex\.)?VertexConsumer\s+(?<vc>\w+)\s*,\s*int\s+(?<pl>\w+)\s*,\s*int\s+(?<po>\w+)\s*,\s*float\s+(?<r>\w+)\s*,\s*float\s+(?<g>\w+)\s*,\s*float\s+(?<b>\w+)\s*,\s*float\s+(?<a>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      $body =~ s/,\s*\Q$c->{r}\E\s*,\s*\Q$c->{g}\E\s*,\s*\Q$c->{b}\E\s*,\s*\Q$c->{a}\E\s*\)/, color)/g;
      return ("$c->{pre}(com.mojang.blaze3d.vertex.PoseStack $c->{ps}, com.mojang.blaze3d.vertex.VertexConsumer $c->{vc}, int $c->{pl}, int $c->{po}, int color) ", $body);
    });

  # --- Block#use -> useWithoutItem
  $src = xform($src,
    qr/(?<pre>\bpublic\s+$ann(?:net\.minecraft\.world\.)?InteractionResult\s+)use\s*\(\s*BlockState\s+(?<s>\w+)\s*,\s*Level\s+(?<l>\w+)\s*,\s*BlockPos\s+(?<p>\w+)\s*,\s*Player\s+(?<pl>\w+)\s*,\s*InteractionHand\s+(?<h>\w+)\s*,\s*BlockHitResult\s+(?<hit>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      $body =~ s/super\.use\(\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*\w+\s*,\s*(\w+)\s*\)/super.useWithoutItem($1, $2, $3, $4, $5)/g;
      return ("$c->{pre}useWithoutItem(BlockState $c->{s}, Level $c->{l}, BlockPos $c->{p}, Player $c->{pl}, BlockHitResult $c->{hit}) ",
              "\n        InteractionHand $c->{h} = InteractionHand.MAIN_HAND;" . $body);
    });

  # --- BlockEntity persistence takes a registry provider
  $src = xform($src,
    qr/(?<pre>\bprotected\s+void\s+saveAdditional\s*)\(\s*CompoundTag\s+(?<t>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my $t = $c->{t};
      $body =~ s/super\.saveAdditional\(\s*\Q$t\E\s*\)/super.saveAdditional($t, registries)/g;
      $body =~ s/(\w+)\.serializeNBT\(\)/$1.serializeNBT(registries)/g;
      return ("$c->{pre}(CompoundTag $t, net.minecraft.core.HolderLookup.Provider registries) ", $body);
    });
  $src = xform($src,
    qr/(?<pre>\bpublic\s+)void\s+load\s*\(\s*CompoundTag\s+(?<t>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my $t = $c->{t};
      return ($sig, $body) unless $body =~ /super\.load\(/ && $src =~ /BlockEntity/;
      $body =~ s/super\.load\(\s*\Q$t\E\s*\)/super.loadAdditional($t, registries)/g;
      $body =~ s/(\w+)\.deserializeNBT\(/$1.deserializeNBT(registries, /g;
      return ("$c->{pre}void loadAdditional(CompoundTag $t, net.minecraft.core.HolderLookup.Provider registries) ", $body);
    });
  $src = xform($src,
    qr/(?<pre>\bpublic\s+CompoundTag\s+getUpdateTag\s*)\(\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      $body =~ s/\bsaveWithoutMetadata\(\s*\)/saveWithoutMetadata(registries)/g;
      return ("$c->{pre}(net.minecraft.core.HolderLookup.Provider registries) ", $body);
    });

  # --- Mob#finalizeSpawn lost the CompoundTag argument
  $src =~ s/(\bfinalizeSpawn\s*\([^;{)]*?),\s*(?:\@[\w.]+\s+)?(?:net\.minecraft\.nbt\.)?CompoundTag\s+\w+\s*\)(\s*\{)/$1)$2/g;
  $src =~ s/(super\.finalizeSpawn\([^;]*?),\s*\w+\s*\)(\s*;)/$1)$2/g if $src =~ /super\.finalizeSpawn\([^;]*,[^;]*,[^;]*,[^;]*,[^;]*\)/;

  # --- simple renames
  $src =~ s/\bprotected\s+float\s+getGravity\s*\(\s*\)/protected double getDefaultGravity()/g;
  $src =~ s/\bpublic\s+int\s+getExperienceReward\s*\(\s*\)/public int getBaseExperienceReward()/g;

  if ($src =~ /HolderLookup\.Provider|net\.minecraft\.core\.HolderLookup/) { }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
