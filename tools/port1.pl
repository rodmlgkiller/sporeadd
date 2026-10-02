#!/usr/bin/perl
# Phase 1 of the 1.20.1 (Forge) -> 1.21.1 (NeoForge) port: mechanical rewrites.
use strict; use warnings;

my @files = @ARGV;

# --- registry name map: ForgeRegistries.X -> BuiltInRegistries.Y
my %reg = (
  ITEMS => 'ITEM', BLOCKS => 'BLOCK', ENTITY_TYPES => 'ENTITY_TYPE', MOB_EFFECTS => 'MOB_EFFECT',
  SOUND_EVENTS => 'SOUND_EVENT', PARTICLE_TYPES => 'PARTICLE_TYPE', BLOCK_ENTITY_TYPES => 'BLOCK_ENTITY_TYPE',
  MENU_TYPES => 'MENU', ATTRIBUTES => 'ATTRIBUTE', FLUIDS => 'FLUID', POTIONS => 'POTION',
  RECIPE_TYPES => 'RECIPE_TYPE', RECIPE_SERIALIZERS => 'RECIPE_SERIALIZER', FLUID_TYPES => 'FLUID_TYPE',
);

# --- ordered package prefix map (specific first)
my @pkg = (
  ['net.minecraftforge.eventbus.api.',        'net.neoforged.bus.api.'],
  ['net.minecraftforge.fml.common.Mod.EventBusSubscriber', 'net.neoforged.fml.common.EventBusSubscriber'],
  ['net.minecraftforge.api.distmarker.',      'net.neoforged.api.distmarker.'],
  ['net.minecraftforge.fml.',                 'net.neoforged.fml.'],
  ['net.minecraftforge.common.MinecraftForge','net.neoforged.neoforge.common.NeoForge'],
  ['net.minecraftforge.',                     'net.neoforged.neoforge.'],
);

sub split_args {
  my ($s) = @_;
  my @a; my $depth = 0; my $cur = ''; my $instr = 0;
  for my $c (split //, $s) {
    if ($c eq '"' ) { $instr = !$instr; }
    if (!$instr) {
      if ($c eq '(' ) { $depth++; }
      elsif ($c eq ')' ) { $depth--; }
      elsif ($c eq ',' && $depth == 0) { push @a, $cur; $cur = ''; next; }
    }
    $cur .= $c;
  }
  push @a, $cur;
  return @a;
}

my $balanced; $balanced = qr/(?:[^()"]|"(?:[^"\\]|\\.)*"|\((??{$balanced})\))*/;

for my $f (@files) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die "$f: $!"; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  # ResourceLocation constructors
  $src =~ s{new\s+(?:net\.minecraft\.resources\.)?ResourceLocation\s*\((${balanced})\)}{
      my $whole = $&; my $args = $1; my @a = split_args($args);
      my $pfx = ($whole =~ /new\s+net\.minecraft\.resources\./) ? 'net.minecraft.resources.' : '';
      if (@a == 2) { $pfx.'ResourceLocation.fromNamespaceAndPath('.$args.')' }
      elsif (@a == 1) { $pfx.'ResourceLocation.parse('.$args.')' }
      else { $whole }
  }ge;

  # registries
  for my $k (keys %reg) {
    my $v = $reg{$k};
    $src =~ s/\b(?:net\.minecraftforge\.registries\.)?ForgeRegistries\.$k\.getValue\(/BuiltInRegistries.$v.get(/g;
    $src =~ s/\b(?:net\.minecraftforge\.registries\.)?ForgeRegistries\.$k\b/BuiltInRegistries.$v/g;
  }

  # event bus
  $src =~ s/\bMinecraftForge\.EVENT_BUS\b/NeoForge.EVENT_BUS/g;
  $src =~ s/net\.minecraftforge\.common\.MinecraftForge\.EVENT_BUS/net.neoforged.neoforge.common.NeoForge.EVENT_BUS/g;

  # @Mod.EventBusSubscriber -> @EventBusSubscriber
  $src =~ s/Mod\.EventBusSubscriber\.Bus\.FORGE/EventBusSubscriber.Bus.GAME/g;
  $src =~ s/Mod\.EventBusSubscriber\.Bus\.MOD/EventBusSubscriber.Bus.MOD/g;
  $src =~ s/\bMod\.EventBusSubscriber\b/EventBusSubscriber/g;

  # package prefixes
  for my $p (@pkg) { my ($o,$n) = @$p; $src =~ s/\Q$o\E/$n/g; }

  # import fix-ups for simple names now in new packages
  $src =~ s/^import net\.neoforged\.neoforge\.registries\.ForgeRegistries;\r?\n//mg;
  $src =~ s/^import net\.neoforged\.neoforge\.common\.NeoForge;\r?\n/import net.neoforged.neoforge.common.NeoForge;\n/mg;

  # add imports if used and missing
  my @need = (
    ['BuiltInRegistries', 'net.minecraft.core.registries.BuiltInRegistries', qr/\bBuiltInRegistries\./],
    ['NeoForge',          'net.neoforged.neoforge.common.NeoForge',          qr/(?<![\w.])NeoForge\.EVENT_BUS/],
    ['EventBusSubscriber','net.neoforged.fml.common.EventBusSubscriber',     qr/\@EventBusSubscriber/],
  );
  for my $n (@need) {
    my ($simple,$fqn,$use) = @$n;
    if ($src =~ $use && $src !~ /^import \Q$fqn\E;/m) {
      $src =~ s/^(package [^;]+;\r?\n)/$1\nimport $fqn;\n/m;
    }
  }

  if ($src ne $orig) {
    open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out;
  }
}
