#!/usr/bin/perl
# Phase 1c: TickEvent split, ForgeConfigSpec->ModConfigSpec, Spore package renames.
use strict; use warnings;

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  # --- Spore package/class renames (1.21.1 build of Spore)
  $src =~ s/com\.Harbinger\.Spore\.Core\./com.Harbinger.Spore.core./g;
  $src =~ s/com\.Harbinger\.Spore\.Damage\.SdamageTypes/com.Harbinger.Spore.core.SdamageTypes/g;
  $src =~ s/com\.Harbinger\.Spore\.sEvents\./com.Harbinger.Spore.Sevents./g;
  $src =~ s/com\.Harbinger\.Spore\.Client\.Renderers\.OrganoidMobRenderer/com.Harbinger.Spore.Client.Special.OrganoidMobRenderer/g;

  # --- config spec
  $src =~ s/\bForgeConfigSpec\b/ModConfigSpec/g;

  # --- TickEvent
  if ($src =~ /TickEvent/) {
    # phase checks (all handlers in this project run at END)
    $src =~ s/^[ \t]*if \((?:event\.phase != (?:net\.neoforged\.neoforge\.event\.)?TickEvent\.Phase\.END|event\.phase == TickEvent\.Phase\.START)\) return;\r?\n//mg;
    $src =~ s/^[ \t]*if \(event\.phase != TickEvent\.Phase\.END\) \{\s*return;\s*\}\r?\n//mg;
    # "A || B" forms: drop the phase part
    $src =~ s/event\.phase != TickEvent\.Phase\.END \|\| //g;
    # positive block form: bare block
    $src =~ s/if \(event\.phase == TickEvent\.Phase\.END\) \{/{/g;

    my %cls = (
      ServerTickEvent => 'net.neoforged.neoforge.event.tick.ServerTickEvent',
      PlayerTickEvent => 'net.neoforged.neoforge.event.tick.PlayerTickEvent',
      LevelTickEvent  => 'net.neoforged.neoforge.event.tick.LevelTickEvent',
      ClientTickEvent => 'net.neoforged.neoforge.client.event.ClientTickEvent',
    );
    my @imports;
    for my $c (sort keys %cls) {
      my $used = 0;
      $used = 1 if $src =~ s/(?:net\.neoforged\.neoforge\.event\.)?TickEvent\.$c\b(?!\.)/$c.Post/g;
      $used = 1 if $src =~ /\bimport net\.neoforged\.neoforge\.event\.TickEvent\.$c;/;
      $src =~ s/^import net\.neoforged\.neoforge\.event\.TickEvent\.$c;\r?\n//mg;
      push @imports, $cls{$c} if $used;
    }
    $src =~ s/^import net\.neoforged\.neoforge\.event\.TickEvent;\r?\n//mg;
    $src =~ s/\bevent\.player\b/event.getEntity()/g if $src =~ /PlayerTickEvent\.Post/;
    $src =~ s/\bevent\.level\b/event.getLevel()/g   if $src =~ /LevelTickEvent\.Post/;
    for my $i (@imports) {
      $src =~ s/^(package [^;]+;\r?\n)/$1\nimport $i;\n/m unless $src =~ /^import \Q$i\E;/m;
    }
  }

  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
