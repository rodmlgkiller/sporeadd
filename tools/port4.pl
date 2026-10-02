#!/usr/bin/perl
# Phase 4: damage events. LivingAttackEvent -> LivingIncomingDamageEvent;
# LivingHurtEvent -> LivingIncomingDamageEvent if the file cancels, else LivingDamageEvent.Pre.
use strict; use warnings;
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  next unless $src =~ /LivingHurtEvent|LivingAttackEvent/;

  my $inc = 'net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent';
  my $dmg = 'net.neoforged.neoforge.event.entity.living.LivingDamageEvent';
  my %imp;

  if ($src =~ /LivingAttackEvent/) {
    $src =~ s/^import [\w.]+\.LivingAttackEvent;\r?\n//mg;
    $src =~ s/\bLivingAttackEvent\b/LivingIncomingDamageEvent/g;
    $imp{$inc} = 1;
  }
  if ($src =~ /LivingHurtEvent/) {
    $src =~ s/^import [\w.]+\.LivingHurtEvent;\r?\n//mg;
    if ($src =~ /setCanceled/) {
      $src =~ s/\bLivingHurtEvent\b/LivingIncomingDamageEvent/g;
      $imp{$inc} = 1;
    } else {
      $src =~ s/\bLivingHurtEvent\b/LivingDamageEvent.Pre/g;
      $src =~ s/\b(event|e|evt)\.getAmount\(\)/$1.getNewDamage()/g;
      $src =~ s/\b(event|e|evt)\.setAmount\(/$1.setNewDamage(/g;
      $imp{$dmg} = 1;
    }
  }
  for my $i (sort keys %imp) {
    next if $src =~ /^import \Q$i\E;/m;
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport $i;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
