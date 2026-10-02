#!/usr/bin/perl
# Phase 13: buffers, damage events, targets, food, misc.
use strict; use warnings;
my $bal = qr/(?:[^()]|\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\))*/;
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  next if $f =~ m{/BufUtil[.]java$};

  # packet buffers
  if ($src =~ /\.(?:writeItem|readItem|writeComponent|readComponent)\(/) {
    $src =~ s/\b(\w+)\.writeItem\(($bal)\)/BufUtil.writeItem($1, $2)/g;
    $src =~ s/\b(\w+)\.readItem\(\)/BufUtil.readItem($1)/g;
    $src =~ s/\b(\w+)\.writeComponent\(($bal)\)/BufUtil.writeComponent($1, $2)/g;
    $src =~ s/\b(\w+)\.readComponent\(\)/BufUtil.readComponent($1)/g;
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport com.sporeadds.sporeaddsmod.util.BufUtil;\n/m unless $src =~ /import com\.sporeadds\.sporeaddsmod\.util\.BufUtil;/;
  }

  # fire / food
  $src =~ s/\b(\w+)\.setSecondsOnFire\(($bal)\)/$1.igniteForSeconds($2)/g;
  $src =~ s/\b(\w+)\.isEdible\(\)/$1.has(net.minecraft.core.component.DataComponents.FOOD)/g;

  # registry getValue leftovers
  $src =~ s/(BuiltInRegistries\.MOB_EFFECT\s*)\.getValue\(($bal)\)/$1.getHolder($2).orElse(null)/g;
  $src =~ s/(BuiltInRegistries\.\w+\s*)\.getValue\(/$1.get(/g;

  # target change event
  $src =~ s/\bevent\.getNewTarget\(\)/event.getNewAboutToBeSetTarget()/g;
  $src =~ s/\bevent\.setNewTarget\(/event.setNewAboutToBeSetTarget(/g;

  # plain LivingDamageEvent handlers
  if ($src =~ /\(\s*LivingDamageEvent\s+(\w+)\s*\)/) {
    my $ev = $1;
    if ($src =~ /\b\Q$ev\E\.setCanceled\(/) {
      $src =~ s/\bLivingDamageEvent(\s+\Q$ev\E\b)/LivingIncomingDamageEvent$1/g;
      $src =~ s/^(package [^;]+;\r?\n)/$1\nimport net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;\n/m
        unless $src =~ /import [\w.]+\.LivingIncomingDamageEvent;/;
    } elsif ($src =~ /\b\Q$ev\E\.setAmount\(/) {
      $src =~ s/\bLivingDamageEvent(\s+\Q$ev\E\b)/LivingDamageEvent.Pre$1/g;
      $src =~ s/\b\Q$ev\E\.getAmount\(\)/$ev.getNewDamage()/g;
      $src =~ s/\b\Q$ev\E\.setAmount\(/$ev.setNewDamage(/g;
    } else {
      $src =~ s/\bLivingDamageEvent(\s+\Q$ev\E\b)/LivingDamageEvent.Post$1/g;
      $src =~ s/\b\Q$ev\E\.getAmount\(\)/$ev.getNewDamage()/g;
    }
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
