package com.fabioad.ddba.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.fabioad.ddba.engine.audio.AudioManager;

/**
 * Assets
 * =============================================================================
 * OBJETIVO DA CLASSE:
 *   Carregar e disponibilizar TODOS os recursos graficos/sonoros do jogo:
 *   texturas fatiadas em regioes, animacoes prontas, fontes e SFX pre-carregados.
 *   Funciona como um "catalogo" central acessado pelo restante do jogo.
 *
 * ALGORITMO DE FATIAMENTO (spritesheet slicing):
 *   Uma folha (spritesheet) e uma unica textura com varios quadros lado a lado.
 *   Fatiamos por geometria fixa (largura/altura do quadro) em TextureRegions e
 *   montamos objetos Animation<TextureRegion> com duracao por quadro.
 *
 * DECISOES DE ARQUITETURA:
 *   - Filtro NEAREST em todas as texturas (pixel art nitida ao escalar).
 *   - Carregamento TOLERANTE A FALHAS: se um arquivo faltar, registra e segue,
 *     evitando crash durante o desenvolvimento (assets sao gerados por ferramenta).
 *   - Animacoes de jogador expostas por getters nomeados (didatico); inimigos e
 *     itens por mapa nome->animacao.
 *
 * PORTABILIDADE:
 *   - SNES: "fatiar spritesheet" corresponde a definir OBJs a partir de tiles de
 *     8x8 na VRAM; a "Animation" vira uma tabela de indices de tile por quadro.
 */
public final class Assets implements Disposable {

    private final Array<Texture> managedTextures = new Array<>();

    // ---- Fonte (placeholder: fonte embutida do LibGDX, escalada e nitida) ----
    private BitmapFont font;

    // ---- Animacoes do jogador (nomeadas) ----
    public Animation<TextureRegion> playerIdle;
    public Animation<TextureRegion> playerWalk;
    public Animation<TextureRegion> playerRun;
    public Animation<TextureRegion> playerJump;
    public Animation<TextureRegion> playerFall;
    public Animation<TextureRegion> playerCrouch;
    public Animation<TextureRegion> playerLookUp;
    public Animation<TextureRegion> playerHurt;
    public Animation<TextureRegion> playerDead;
    public Animation<TextureRegion> playerVictory;

    // ---- Itens ----
    public Animation<TextureRegion> coin;

    /** Barraca/"castelo" de fim de fase (apos a bandeira). */
    public TextureRegion castle;

    // ---- Inimigos (nome -> animacao de andar/voar) ----
    public final ObjectMap<String, Animation<TextureRegion>> enemyAnims = new ObjectMap<>();

    // ---- Tiles (indexados pelo id do tile) ----
    public TextureRegion[] tiles;

    // ---- Fundos por fase ----
    public final TextureRegion[] backgrounds = new TextureRegion[AssetPaths.BACKGROUNDS.length];

    /** Fundo tela cheia do menu e da apresentacao (rua/Daniel), ja escurecido. */
    public TextureRegion menuBg;

    /** Foto final do iate (ending / creditos / foto limpa). */
    public TextureRegion endingBg;

    private final AudioManager audio;

    public Assets(AudioManager audio) {
        this.audio = audio;
    }

    /** Carrega todos os recursos. Deve ser chamado na tela de boot. */
    public void load() {
        loadFont();
        loadPlayer();
        loadItems();
        loadEnemies();
        loadTiles();
        loadBackgrounds();
        loadSfx();
    }

    private void loadFont() {
        // BMFont pixelada (Pixelon, sem AA) — escalas inteiras (1x/2x) no canvas 256x224.
        if (Gdx.files.internal(AssetPaths.FONT_PIXEL).exists()) {
            font = new BitmapFont(Gdx.files.internal(AssetPaths.FONT_PIXEL));
        } else {
            Gdx.app.log("Assets", "Fonte pixel ausente; usando fallback LibGDX.");
            font = new BitmapFont();
        }
        font.setUseIntegerPositions(true);
        for (int i = 0; i < font.getRegions().size; i++) {
            font.getRegion(i).getTexture().setFilter(
                    Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    private void loadPlayer() {
        Texture tex = loadTexture(AssetPaths.PLAYER);
        if (tex == null) return;
        final int fw = 24, fh = 32; // largura/altura do quadro
        TextureRegion[] f = slice(tex, fw, fh);
        // Faixas de quadros por animacao (ver gerador de assets).
        playerIdle = anim(0.30f, Animation.PlayMode.LOOP, f, 0, 1);
        playerWalk = anim(0.12f, Animation.PlayMode.LOOP, f, 2, 5);
        playerRun = anim(0.08f, Animation.PlayMode.LOOP, f, 6, 9);
        playerJump = anim(0.10f, Animation.PlayMode.NORMAL, f, 10, 10);
        playerFall = anim(0.10f, Animation.PlayMode.NORMAL, f, 11, 11);
        playerCrouch = anim(0.10f, Animation.PlayMode.NORMAL, f, 12, 12);
        playerLookUp = anim(0.10f, Animation.PlayMode.NORMAL, f, 13, 13);
        playerHurt = anim(0.10f, Animation.PlayMode.NORMAL, f, 14, 14);
        playerDead = anim(0.15f, Animation.PlayMode.NORMAL, f, 15, 15);
        playerVictory = anim(0.20f, Animation.PlayMode.LOOP, f, 16, 17);
    }

    private void loadItems() {
        Texture tex = loadTexture(AssetPaths.COIN);
        if (tex != null) {
            coin = anim(0.10f, Animation.PlayMode.LOOP, slice(tex, 16, 16), 0, 3);
        }
        Texture castleTex = loadTexture(AssetPaths.CASTLE);
        if (castleTex != null) {
            castle = new TextureRegion(castleTex);
        }
    }

    private void loadEnemies() {
        putEnemy("walker", AssetPaths.ENEMY_WALKER, 24, 24, 0.15f);
        putEnemy("flyer", AssetPaths.ENEMY_FLYER, 16, 16, 0.20f);
        putEnemy("fast", AssetPaths.ENEMY_FAST, 16, 16, 0.08f);
        putEnemy("tank", AssetPaths.ENEMY_TANK, 24, 24, 0.25f);
        putEnemy("boss", AssetPaths.BOSS, 48, 48, 0.30f);
    }

    private void putEnemy(String key, String path, int fw, int fh, float frameTime) {
        Texture tex = loadTexture(path);
        if (tex == null) return;
        TextureRegion[] f = slice(tex, fw, fh);
        enemyAnims.put(key, new Animation<>(frameTime, new Array<>(f), Animation.PlayMode.LOOP));
    }

    private void loadTiles() {
        Texture tex = loadTexture(AssetPaths.TILESET);
        if (tex != null) {
            tiles = slice(tex, 16, 16);
        } else {
            tiles = new TextureRegion[0];
        }
    }

    private void loadBackgrounds() {
        for (int i = 0; i < AssetPaths.BACKGROUNDS.length; i++) {
            Texture tex = loadTexture(AssetPaths.BACKGROUNDS[i]);
            if (tex != null) backgrounds[i] = new TextureRegion(tex);
        }
        Texture menu = loadTexture(AssetPaths.MENU_BG);
        if (menu != null) {
            menuBg = new TextureRegion(menu);
        }
        Texture ending = loadTexture(AssetPaths.ENDING_BG);
        if (ending != null) {
            endingBg = new TextureRegion(ending);
        }
    }

    private void loadSfx() {
        audio.loadSfx("jump", AssetPaths.SFX_JUMP);
        audio.loadSfx("coin", AssetPaths.SFX_COIN);
        audio.loadSfx("hurt", AssetPaths.SFX_HURT);
        audio.loadSfx("defeat", AssetPaths.SFX_DEFEAT);
        audio.loadSfx("break", AssetPaths.SFX_BREAK);
        audio.loadSfx("victory", AssetPaths.SFX_VICTORY);
        audio.loadSfx("gameover", AssetPaths.SFX_GAMEOVER);
        audio.loadSfx("menu", AssetPaths.SFX_MENU);
        audio.loadSfx("select", AssetPaths.SFX_SELECT);
        audio.loadSfx("boss", AssetPaths.SFX_BOSS);
    }

    // ----------------------- utilitarios de carregamento ---------------------

    private Texture loadTexture(String path) {
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.log("Assets", "Textura ausente (ignorada): " + path);
            return null;
        }
        Texture t = new Texture(Gdx.files.internal(path));
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        managedTextures.add(t);
        return t;
    }

    /** Fatia uma textura horizontalmente em quadros de fw x fh (uma linha). */
    private static TextureRegion[] slice(Texture tex, int fw, int fh) {
        int cols = tex.getWidth() / fw;
        TextureRegion[] out = new TextureRegion[cols];
        for (int i = 0; i < cols; i++) {
            out[i] = new TextureRegion(tex, i * fw, 0, fw, fh);
        }
        return out;
    }

    /** Monta uma Animation a partir de um intervalo [from..to] de quadros. */
    private static Animation<TextureRegion> anim(float frameTime, Animation.PlayMode mode,
                                                 TextureRegion[] frames, int from, int to) {
        Array<TextureRegion> a = new Array<>();
        for (int i = from; i <= to && i < frames.length; i++) {
            a.add(frames[i]);
        }
        if (a.size == 0 && frames.length > 0) a.add(frames[0]);
        Animation<TextureRegion> anim = new Animation<>(frameTime, a);
        anim.setPlayMode(mode);
        return anim;
    }

    public BitmapFont getFont() {
        return font;
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        for (Texture t : managedTextures) {
            t.dispose();
        }
        managedTextures.clear();
    }
}
