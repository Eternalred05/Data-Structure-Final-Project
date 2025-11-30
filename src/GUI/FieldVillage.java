package GUI;

import Runner.MainScreen;
import Characters.Hero;
import Logic.Game;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FieldVillage {

    private final StackPane root;
    private final Pane world;
    private final StackPane loadingOverlay;
    private ImageView backgroundView;
    private MediaPlayer music;

    private final ImageView heroView;
    private final double HERO_W = 48;
    private final double HERO_H = 48;
    private final double HERO_SPEED = 180.0;
    private final Set<KeyCode> keys = new HashSet<>();
    private AnimationTimer mover;

    private final double VIEW_W = 800;
    private final double VIEW_H = 600;
    private double worldW = VIEW_W;
    private double worldH = VIEW_H;

    private Rectangle startRect;
    private boolean onStartRect = false;

    private Runnable onExitCallback;
    private final Game game;

    public FieldVillage(Game game) {
        this.game = game;
        root = new StackPane();
        root.setPrefSize(VIEW_W, VIEW_H);

        world = new Pane();
        world.setPrefSize(VIEW_W, VIEW_H);

        loadingOverlay = createLoadingOverlay();

        root.getChildren().addAll(world, loadingOverlay);

        heroView = createHeroView();

        installInputHandlers();
        createMover();

        // Limpia inputs al perder foco
        root.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) clearInputState();
        });
    }

    public void showWithLoading(Runnable onLoaded, Runnable onExit) {
        this.onExitCallback = onExit;

        Platform.runLater(() -> {
            FXGL.getGameScene().addUINode(root);
            showLoading(true);

            boolean imageOk = loadBackgroundImage("/Resources/textures/fieldVillage.png");
            boolean musicOk = startVillageMusic("/Resources/music/fieldVillage.mp3");

            positionHeroAtBottomCenter();
            createStartRectAtHeroStart();

            PauseTransition wait = new PauseTransition(Duration.millis(600));
            wait.setOnFinished(e -> {
                showLoading(false);
                fadeInContent();
                startMover();
                if (onLoaded != null) onLoaded.run();
            });
            wait.play();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            stopVillageMusic();
            stopMover();
            try { FXGL.getGameScene().removeUINode(root); } catch (Throwable ignored) {}
        });
    }

    private StackPane createLoadingOverlay() {
        StackPane overlay = new StackPane();
        overlay.setPickOnBounds(true);

        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(VIEW_W, VIEW_H);
        bg.setFill(Color.rgb(0, 0, 0, 0.6));

        Text label = new Text("Cargando aldea...");
        label.setStyle("-fx-font-size: 24px; -fx-fill: #e0d090;");

        overlay.getChildren().addAll(bg, label);
        StackPane.setAlignment(label, Pos.CENTER);
        overlay.setVisible(false);
        return overlay;
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        if (show) loadingOverlay.toFront();
        else loadingOverlay.toBack();
    }

    private void fadeInContent() {
        FadeTransition ft = new FadeTransition(Duration.millis(400), root);
        ft.setFromValue(0.2);
        ft.setToValue(1.0);
        ft.play();
    }

    private boolean loadBackgroundImage(String path) {
        try {
            Image img = new Image(getClass().getResourceAsStream(path));
            backgroundView = new ImageView(img);
            backgroundView.setPreserveRatio(false);
            backgroundView.setSmooth(true);

            worldW = img.getWidth() > 0 ? img.getWidth() : VIEW_W;
            worldH = img.getHeight() > 0 ? img.getHeight() : VIEW_H;

            backgroundView.setFitWidth(worldW);
            backgroundView.setFitHeight(worldH);

            world.setPrefSize(worldW, worldH);
            world.getChildren().clear();
            world.getChildren().add(backgroundView);

            if (!world.getChildren().contains(heroView)) {
                world.getChildren().add(heroView);
            } else {
                heroView.toFront();
            }
            return true;
        } catch (Throwable t) {
            Text err = new Text("No se pudo cargar la imagen de la aldea.");
            err.setStyle("-fx-font-size: 16px; -fx-fill: #ffdddd;");
            root.getChildren().add(err);
            return false;
        }
    }

    private boolean startVillageMusic(String path) {
        try {
            URL res = getClass().getResource(path);
            if (res == null) return false;
            Media media = new Media(res.toExternalForm());
            stopVillageMusic();
            music = new MediaPlayer(media);
            music.setCycleCount(MediaPlayer.INDEFINITE);
            music.setVolume(0.8);
            music.play();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private void stopVillageMusic() {
        try {
            if (music != null) {
                music.stop();
                music.dispose();
                music = null;
            }
        } catch (Throwable ignored) {}
    }

    private ImageView createHeroView() {
        Image img = null;
        try { img = new Image(getClass().getResourceAsStream("/Resources/sprites/hero.png")); }
        catch (Throwable ignored) { img = null; }
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(HERO_W);
        iv.setFitHeight(HERO_H);
        iv.setMouseTransparent(true);
        return iv;
    }

    private void positionHeroAtBottomCenter() {
        double startX = (worldW - HERO_W) / 2.0;
        double startY = worldH - HERO_H - 8.0;
        startX = clamp(startX, 0, Math.max(0, worldW - HERO_W));
        startY = clamp(startY, 0, Math.max(0, worldH - HERO_H));
        heroView.setLayoutX(startX);
        heroView.setLayoutY(startY);
        updateCamera();
    }

    private void createStartRectAtHeroStart() {
        if (startRect != null) {
            world.getChildren().remove(startRect);
            startRect = null;
        }
        double rx = heroView.getLayoutX();
        double ry = heroView.getLayoutY();
        double rw = HERO_W + 8;
        double rh = HERO_H + 8;

        startRect = new Rectangle(rx - 4, ry - 4, rw, rh);
        startRect.setFill(Color.rgb(0, 120, 255, 0.28));
        startRect.setStroke(Color.rgb(0, 80, 200, 0.9));
        startRect.setMouseTransparent(true);

        if (!world.getChildren().contains(startRect)) {
            world.getChildren().add(startRect);
        }
        startRect.toBack();
        heroView.toFront();
    }

    private void installInputHandlers() {
        root.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            KeyCode k = ev.getCode();
            if (k == KeyCode.W || k == KeyCode.UP) keys.add(KeyCode.W);
            if (k == KeyCode.S || k == KeyCode.DOWN) keys.add(KeyCode.S);
            if (k == KeyCode.A || k == KeyCode.LEFT) keys.add(KeyCode.A);
            if (k == KeyCode.D || k == KeyCode.RIGHT) keys.add(KeyCode.D);

            if (k == KeyCode.ENTER) {
                if (onStartRect) {
                    // limpiar inputs antes de salir
                    clearInputState();
                    try {
                        if (game != null && game.getHero() != null) {
                            Hero h = game.getHero();
                            h.setLastLocation(Hero.Location.FIELD_VILLAGE);
                            h.setLastPosX(heroView.getLayoutX());
                            h.setLastPosY(heroView.getLayoutY());
                            try { game.createSaveGame(); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                    if (onExitCallback != null) {
                        hide();
                        onExitCallback.run();
                    } else {
                        hide();
                    }
                }
            }

            if (k == KeyCode.ESCAPE) {
                // limpiar inputs antes del diálogo
                clearInputState();

                Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
                dlg.setTitle("Volver al menú");
                dlg.setHeaderText("¿Quieres volver al menú principal?");
                dlg.setContentText("Si vuelves al menú, la partida seguirá guardada en disco.");
                try {
                    if (root.getScene() != null && root.getScene().getWindow() != null) {
                        dlg.initOwner(root.getScene().getWindow());
                    }
                } catch (Throwable ignored) {}
                dlg.setOnHidden(eh -> {
                    clearInputState();
                    Platform.runLater(root::requestFocus);
                });

                Optional<ButtonType> opt = dlg.showAndWait();
                boolean ok = opt.isPresent() && opt.get() == ButtonType.OK;
                if (ok) {
                    try {
                        if (game != null && game.getHero() != null) {
                            Hero h = game.getHero();
                            h.setLastLocation(Hero.Location.FIELD_VILLAGE);
                            h.setLastPosX(heroView.getLayoutX());
                            h.setLastPosY(heroView.getLayoutY());
                            try { game.createSaveGame(); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}

                    stopVillageMusic();
                    try { FXGL.getGameScene().removeUINode(root); } catch (Throwable ignored) {}
                    MainScreen.restoreMenuAndMusic();
                } else {
                    clearInputState();
                    Platform.runLater(root::requestFocus);
                }
            }

            ev.consume();
        });

        root.addEventFilter(KeyEvent.KEY_RELEASED, ev -> {
            KeyCode k = ev.getCode();
            if (k == KeyCode.W || k == KeyCode.UP) keys.remove(KeyCode.W);
            if (k == KeyCode.S || k == KeyCode.DOWN) keys.remove(KeyCode.S);
            if (k == KeyCode.A || k == KeyCode.LEFT) keys.remove(KeyCode.A);
            if (k == KeyCode.D || k == KeyCode.RIGHT) keys.remove(KeyCode.D);
            ev.consume();
        });

        root.setFocusTraversable(true);
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) Platform.runLater(root::requestFocus);
            else clearInputState();
        });
    }

    private void createMover() {
        mover = new AnimationTimer() {
            private long last = -1;
            @Override
            public void handle(long now) {
                if (last < 0) last = now;
                double dt = (now - last) / 1e9;
                last = now;

                // Watchdog: si no hay foco, no mover y limpiar inputs
                if (root.getScene() == null || !root.isFocused()) {
                    clearInputState();
                    return;
                }

                updateAndMove(dt);
            }
        };
    }

    private void startMover() {
        if (mover != null) mover.start();
    }

    private void stopMover() {
        if (mover != null) mover.stop();
    }

    private void updateAndMove(double dt) {
        double vx = 0;
        double vy = 0;
        if (keys.contains(KeyCode.A)) vx -= HERO_SPEED;
        if (keys.contains(KeyCode.D)) vx += HERO_SPEED;
        if (keys.contains(KeyCode.W)) vy -= HERO_SPEED;
        if (keys.contains(KeyCode.S)) vy += HERO_SPEED;

        if (vx == 0 && vy == 0) {
            checkStartIntersection();
            return;
        }

        moveHero(vx * dt, vy * dt);
    }

    private void moveHero(double dx, double dy) {
        double curX = heroView.getLayoutX();
        double curY = heroView.getLayoutY();

        double proposedX = clamp(curX + dx, 0, Math.max(0, worldW - HERO_W));
        double proposedY = clamp(curY + dy, 0, Math.max(0, worldH - HERO_H));

        heroView.setLayoutX(proposedX);
        heroView.setLayoutY(proposedY);

        checkStartIntersection();
        updateCamera();
    }

    private void checkStartIntersection() {
        if (startRect == null) {
            onStartRect = false;
            return;
        }
        boolean intersects = heroView.getBoundsInParent().intersects(startRect.getBoundsInParent());
        onStartRect = intersects;
        startRect.setFill(intersects ? Color.rgb(0, 120, 255, 0.42) : Color.rgb(0, 120, 255, 0.28));
    }

    private void updateCamera() {
        double heroCenterX = heroView.getLayoutX() + HERO_W / 2.0;
        double heroCenterY = heroView.getLayoutY() + HERO_H / 2.0;

        double targetTx = VIEW_W / 2.0 - heroCenterX;
        double targetTy = VIEW_H / 2.0 - heroCenterY;

        double minTx = Math.min(0, VIEW_W - worldW);
        double maxTx = 0;
        double minTy = Math.min(0, VIEW_H - worldH);
        double maxTy = 0;

        double tx = clamp(targetTx, minTx, maxTx);
        double ty = clamp(targetTy, minTy, maxTy);

        double lowerZone = worldH * 0.75;
        if (heroCenterY > lowerZone) {
            double factor = 0.45;
            ty = ty * factor + (VIEW_H / 2.0 - heroCenterY) * (1 - factor);
            ty = clamp(ty, minTy, maxTy);
        }

        world.setTranslateX(tx);
        world.setTranslateY(ty);
    }

    private static double clamp(double v, double lo, double hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }

    public void setHeroPosition(double x, double y) {
        double nx = clamp(x, 0, Math.max(0, worldW - HERO_W));
        double ny = clamp(y, 0, Math.max(0, worldH - HERO_H));
        heroView.setLayoutX(nx);
        heroView.setLayoutY(ny);
        updateCamera();
    }

    private void clearInputState() {
        keys.clear();
    }
}
