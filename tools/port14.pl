#!/usr/bin/perl
# Phase 14: client API changes.
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

for my $f (@ARGV) {
  open(my $fh, '<:encoding(UTF-8)', $f) or die; local $/; my $src = <$fh>; close $fh;
  my $orig = $src;

  # item handler capability on the block entities
  $src =~ s/(?:net\.neoforged\.neoforge\.common\.capabilities\.)?ForgeCapabilities\.ITEM_HANDLER\.get\(([\w.]+)\)/java.util.Optional.of($1.getItemHandler())/g;
  $src =~ s/^import [\w.]*ForgeCapabilities;\r?\n//mg;

  # menus
  $src =~ s/\bIForgeMenuType\b/IMenuTypeExtension/g;
  $src =~ s/net\.neoforged\.neoforge\.common\.extensions\.IMenuTypeExtension/net.neoforged.neoforge.common.extensions.IMenuTypeExtension/g;

  # window / skin helpers
  $src =~ s/\bevent\.getWindow\(\)/net.minecraft.client.Minecraft.getInstance().getWindow()/g;
  $src =~ s/"slim"\.equals\((\w+)\.getModelName\(\)\)/\$1.getSkin().model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM/g;
  $src =~ s/\$1\.getSkin/__X__/g if 0;
  $src =~ s/\bmc\.getFrameTime\(\)/mc.getTimer().getGameTimeDeltaPartialTick(false)/g;

  # renderBackground(g) inside render(g, a, b, c)
  $src = xform($src,
    qr/(?<pre>\bpublic\s+void\s+render\s*)\(\s*(?:net\.minecraft\.client\.gui\.)?GuiGraphics\s+(?<g>\w+)\s*,\s*int\s+(?<a>\w+)\s*,\s*int\s+(?<b>\w+)\s*,\s*float\s+(?<c>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my ($g, $a, $b, $p) = @$c{qw(g a b c)};
      if ($body =~ /\bsuper\.render\(/) {
        $body =~ s/^[ \t]*(?:this\.)?renderBackground\(\s*\Q$g\E\s*\);[ \t]*\r?\n//mg;
      } else {
        $body =~ s/(?:this\.)?renderBackground\(\s*\Q$g\E\s*\)/renderBackground($g, $a, $b, $p)/g;
      }
      return ($sig . ' ', $body);
    });

  # mouseScrolled
  $src = xform($src,
    qr/(?<pre>\bpublic\s+boolean\s+mouseScrolled\s*)\(\s*double\s+(?<a>\w+)\s*,\s*double\s+(?<b>\w+)\s*,\s*double\s+(?<d>\w+)\s*\)\s*\{/,
    sub {
      my ($sig, $body, $c) = @_;
      my ($a, $b, $d) = @$c{qw(a b d)};
      $body =~ s/super\.mouseScrolled\(\s*\Q$a\E\s*,\s*\Q$b\E\s*,\s*\Q$d\E\s*\)/super.mouseScrolled($a, $b, scrollX, scrollY)/g;
      $body =~ s/\b\Q$d\E\b/scrollY/g;
      return ("$c->{pre}(double $a, double $b, double scrollX, double scrollY) ", $body);
    });

  # renderToBuffer(.., 1,1,1,1) calls
  $src =~ s/(\.renderToBuffer\([^;]*?),\s*1(?:\.0[fF]?|[fF])?\s*,\s*1(?:\.0[fF]?|[fF])?\s*,\s*1(?:\.0[fF]?|[fF])?\s*,\s*1(?:\.0[fF]?|[fF])?\s*\)/$1, -1)/g;

  # vertex builder chains
  if ($src =~ /\.vertex\(/ && $src !~ /ClientXRayHandler|GasSphereRenderer/) {
    $src =~ s/\.vertex\(/.addVertex(/g;
    $src =~ s/\.overlayCoords\(/.setOverlay(/g;
    $src =~ s/\.uv2\(/.setLight(/g;
    $src =~ s/\.endVertex\(\)//g;
    $src =~ s/\.addVertex\(([^;]*?)\)\.color\(/.addVertex($1).setColor(/g;
    $src =~ s/\)\.color\(/).setColor(/g;
    $src =~ s/\)\.uv\(/).setUv(/g;
    $src =~ s/\)\.normal\(/).setNormal(/g;
  }

  # GUI overlays -> layers
  if ($src =~ /\bIGuiOverlay\b/) {
    $src =~ s/net\.neoforged\.neoforge\.client\.gui\.overlay\.IGuiOverlay/net.minecraft.client.gui.LayeredDraw.Layer/g;
    $src =~ s/^import net\.minecraft\.client\.gui\.LayeredDraw\.Layer;\r?\n//mg;
    $src =~ s/\bIGuiOverlay\b/net.minecraft.client.gui.LayeredDraw.Layer/g;
    $src =~ s/\(\s*gui\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*\)\s*->\s*\{/($1, deltaTracker) -> {\n        float $2 = deltaTracker.getGameTimeDeltaPartialTick(false);\n        int $3 = $1.guiWidth();\n        int $4 = $1.guiHeight();/g;
  }

  # player skin models
  $src =~ s/for \(String skin : event\.getSkins\(\)\)/for (net.minecraft.client.resources.PlayerSkin.Model skin : event.getSkins())/g;
  $src =~ s/event\.getSkin\("default"\)/event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.WIDE)/g;
  $src =~ s/event\.getSkin\("slim"\)/event.getSkin(net.minecraft.client.resources.PlayerSkin.Model.SLIM)/g;

  if ($src ne $orig) { open(my $out, '>:encoding(UTF-8)', $f) or die; print $out $src; close $out; }
}
