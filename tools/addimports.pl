#!/usr/bin/perl
# add missing imports for a few commonly used simple names
use strict; use warnings;
my %imp = (
  ResourceLocation => 'net.minecraft.resources.ResourceLocation',
  Holder => "net.minecraft.core.Holder",
  BuiltInRegistries => "net.minecraft.core.registries.BuiltInRegistries",
  SubscribeEvent => "net.neoforged.bus.api.SubscribeEvent",
);
for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;
  (my $code = $src) =~ s{//[^\n]*}{}g;
  $code =~ s{^import [^\n]*\n}{}mg;
  for my $n (sort keys %imp) {
    my $fqn = $imp{$n};
    next if $src =~ /^import \Q$fqn\E;/m;
    next if $src =~ /^import [\w.]+\.\Q$n\E;/m;
    next if $src =~ /^import net\.minecraft\.resources\.\*;/m && $n eq 'ResourceLocation';
    next if $src =~ /\b(?:class|interface|enum|record)\s+\Q$n\E\b/;
    next unless $code =~ /(?<![.\w])\Q$n\E\b/;
    $src =~ s/^(package [^;]+;\r?\n)/$1\nimport $fqn;\n/m;
  }
  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
