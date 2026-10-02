#!/usr/bin/perl
# Phase 1b: RegistryObject<T> NAME = REG.register(...)  ->  DeferredHolder<R, T> NAME = REG.register(...)
use strict; use warnings;
my $angle = qr/<((?:[^<>]|<(?-1)>)*)>/;

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  next unless $src =~ /RegistryObject/;

  my %r;
  while ($src =~ /DeferredRegister\s*<((?:[^<>]|<(?-1)>)*)>\s+(\w+)\s*=/g) { $r{$2} = $1; }
  # also DeferredRegister.Items etc. are not used in this project

  $src =~ s{RegistryObject\s*<((?:[^<>]|<(?-1)>)*)>(\s+\w+\s*=\s*)(\w+)\.register\(}{
      my ($t,$mid,$var) = ($1,$2,$3);
      my $reg = $r{$var};
      defined $reg ? "DeferredHolder<$reg, $t>$mid$var.register(" : "RegistryObject<$t>$mid$var.register(";
  }ge;

  if ($src =~ /DeferredHolder/ && $src !~ /import net\.neoforged\.neoforge\.registries\.DeferredHolder;/) {
    $src =~ s/^import net\.neoforged\.neoforge\.registries\.RegistryObject;\r?\n/import net.neoforged.neoforge.registries.DeferredHolder;\n/m
      or $src =~ s/^(package [^;]+;\r?\n)/$1\nimport net.neoforged.neoforge.registries.DeferredHolder;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
