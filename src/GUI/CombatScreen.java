package GUI;

import Logic.Game;
import Characters.Monster;
import Characters.Hero;
import Items.Weapon;
import Misc.Classes;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import Runner.MainScreen;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CombatScreen actualizado: - Héroe fijo a la izquierda (usa la imagen del Hero
 * si está disponible). - Monstruos alineados a la derecha del centro en fila. -
 * Botones de acción abajo; flechas para seleccionar, Enter para ejecutar. -
 * Battle/Item/Defend muestran alertas de éxito. - Al ejecutar Battle el héroe
 * ataca; luego cada monstruo vivo ataca al héroe. - Tras cada ataque se revisa
 * si alguien murió: - Si un monstruo muere y ya no quedan monstruos, el combate
 * termina y se vuelve al mapa (onExit). - Si el héroe muere, se muestra
 * pantalla Game Over (gameOver.png + gameOver.mp3). El botón Start lleva al
 * menú principal.
 */
public class CombatScreen {

    public final StackPane root;
    private final Pane content;
    private final ImageView backgroundView;

    // Layout regions
    private final Pane leftPane;      // para héroe fijo
    private final Pane centerPane;    // espacio central (vacío)
    private final Pane rightPane;     // para monstruos (a la derecha del centro)
    private final HBox monstersBox;   // fila de monstruos
    private final HBox actionButtons; // botones de acción

    private final Game game;
    private final List<Monster> monsters = new ArrayList<>();
    private final List<ImageView> monsterViews = new ArrayList<>();
    private final Random rnd = new Random();

    // Botones y selección por teclado
    private final List<Button> buttons = new ArrayList<>();
    private int selectedButtonIndex = 0;

    private Runnable onExit; // callback opcional al cerrar combate

    // Game Over UI
    private Pane gameOverOverlay = null;
    private MediaPlayer gameOverPlayer = null;

    /**
     * @param game instancia del juego (puede ser null para debug)
     * @param bgPath ruta del fondo (ej: "/Resources/textures/battle_bg.png")
     * @param monsterSpritePaths lista de rutas de sprites para elegir
     * aleatoriamente
     * @param heroForIcon héroe para usar su imagen en el icono (puede ser null)
     */
    public CombatScreen(Game game, String bgPath, List<String> monsterSpritePaths, Hero heroForIcon) {
        this.game = game;

        root = new StackPane();
        root.setPrefSize(800, 600);

        // Fondo
        Image bg = null;
        try {
            bg = new Image(getClass().getResourceAsStream(bgPath));
        } catch (Throwable ignored) {
        }
        backgroundView = new ImageView(bg);
        backgroundView.setPreserveRatio(false);
        backgroundView.setFitWidth(800);
        backgroundView.setFitHeight(600);

        content = new Pane();
        content.setPrefSize(800, 600);

        // Paneles: left (hero), center (spacer), right (monsters)
        leftPane = new Pane();
        leftPane.setPrefSize(220, 600);
        leftPane.setLayoutX(0);
        leftPane.setLayoutY(0);

        centerPane = new Pane();
        centerPane.setPrefSize(360, 600);
        centerPane.setLayoutX(220);
        centerPane.setLayoutY(0);

        rightPane = new Pane();
        rightPane.setPrefSize(220, 600);
        rightPane.setLayoutX(580);
        rightPane.setLayoutY(0);

        // Monstruos: fila dentro del rightPane, alineada al centro vertical y a la derecha horizontalmente
        monstersBox = new HBox(12);
        monstersBox.setAlignment(Pos.CENTER_RIGHT);
        monstersBox.setPrefWidth(200);
        monstersBox.setLayoutX(10);
        monstersBox.setLayoutY(120);
        rightPane.getChildren().add(monstersBox);

        // Panel inferior oscuro
        Rectangle bottomPanel = new Rectangle(800, 96, Color.rgb(10, 10, 10, 0.86));
        bottomPanel.setLayoutX(0);
        bottomPanel.setLayoutY(504);

        actionButtons = new HBox(12);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(12));
        actionButtons.setLayoutX(0);
        actionButtons.setLayoutY(520);
        actionButtons.setPrefWidth(800);

        content.getChildren().addAll(backgroundView, leftPane, centerPane, rightPane, bottomPanel, actionButtons);
        root.getChildren().add(content);

        // Héroe: icono fijo, centrado verticalmente dentro de leftPane
        createHeroIcon(heroForIcon);

        // Generar entre 1 y 3 monstruos y colocarlos en monstersBox (fila, alineados a la derecha del centro)
        int count = 1 + rnd.nextInt(3);
        for (int i = 0; i < count; i++) {
            String sprite = chooseRandomSprite(monsterSpritePaths);
            Monster m = createDebugMonster(sprite, "Monstruo " + (i + 1));
            monsters.add(m);
            ImageView mv = createMonsterView(m);
            monsterViews.add(mv);
            VBox wrapper = new VBox(6);
            wrapper.setAlignment(Pos.CENTER);
            Text name = new Text(m.getName());
            name.setFill(Color.WHITE);
            name.setFont(Font.font(12));
            wrapper.getChildren().addAll(mv, name);
            wrapper.setMouseTransparent(true);
            monstersBox.getChildren().add(wrapper);
        }

        // Crear botones y lógica de selección
        createActionButtons();

        // Instalar manejadores de teclado para controlar botones
        installKeyHandlers();

        // Estética y foco
        root.setCursor(Cursor.DEFAULT);
        Platform.runLater(() -> root.requestFocus());
    }

    private String chooseRandomSprite(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return "/Resources/sprites/monster1.png";
        }
        return paths.get(rnd.nextInt(paths.size()));
    }

    private Monster createDebugMonster(String spritePath, String name) {
        Weapon w = (Weapon) game.getItems().get(1);
        int attack = 4;
        int magic = 0;
        int defense = 1;
        int velocidad = 6;
        int level = 1;
        int life = 12;
        int actualLife = 12;
        Monster m = new Monster(w, attack, magic, defense, velocidad, level, name, spritePath, life, actualLife);
        return m;
    }

    private ImageView createMonsterView(Monster m) {
        Image img = m.getFxImage();
        if (img == null) {
            try {
                img = new Image(getClass().getResourceAsStream("/Resources/sprites/monster1.png"));
            } catch (Throwable ignored) {
            }
        }
        ImageView iv = new ImageView(img);
        iv.setPreserveRatio(true);
        iv.setFitWidth(96);
        iv.setFitHeight(96);
        iv.setSmooth(true);
        return iv;
    }

    private void createHeroIcon(Hero heroForIcon) {
        Image heroImg = null;
        if (heroForIcon != null) {
            try {
                heroImg = heroForIcon.getImage();
            } catch (Throwable ignored) {
            }
        }
        if (heroImg == null && game != null && game.getHero() != null) {
            try {
                heroImg = game.getHero().getImage();
            } catch (Throwable ignored) {
            }
        }
        if (heroImg == null) {
            try {
                heroImg = new Image(getClass().getResourceAsStream("/Resources/sprites/hero.png"));
            } catch (Throwable ignored) {
            }
        }
        ImageView heroIv = new ImageView(heroImg);
        heroIv.setPreserveRatio(true);
        heroIv.setFitWidth(120);
        heroIv.setFitHeight(120);
        heroIv.setSmooth(true);

        double leftMargin = 40;
        double paneHeight = leftPane.getPrefHeight();
        double ivHeight = 120;
        double layoutY = (paneHeight - ivHeight) / 2.0;
        heroIv.setLayoutX(leftMargin);
        heroIv.setLayoutY(layoutY);

        Text name = new Text((game != null && game.getHero() != null) ? game.getHero().getName() : (heroForIcon != null ? heroForIcon.getName() : "Heroe"));
        name.setFill(Color.WHITE);
        name.setFont(Font.font(14));
        name.setLayoutX(leftMargin);
        name.setLayoutY(layoutY + ivHeight + 18);

        leftPane.getChildren().addAll(heroIv, name);
    }

    private void createActionButtons() {
        Button bBattle = styledButton("Battle");
        Button bItem = styledButton("Item");
        Button bDefend = styledButton("Defend");
        Button bEscape = styledButton("Escape");

        buttons.add(bBattle);
        buttons.add(bItem);
        buttons.add(bDefend);
        buttons.add(bEscape);

        // Acciones: Battle/Item/Defend muestran alerta de éxito; Battle además ejecuta combate
        bBattle.setOnAction(e -> {
            // Hero attacks first
            onBattle();
        });
        bItem.setOnAction(e -> {
            showSuccessAlert("Item", "La acción Item se ejecutó correctamente.");
            // After using item you might want monsters to attack as well (optional)
            monstersAttackAfterHeroAction();
        });
        bDefend.setOnAction(e -> {
            showSuccessAlert("Defend", "La acción Defend se ejecutó correctamente.");
            // After defend, monsters attack (defend effect not implemented here)
            monstersAttackAfterHeroAction();
        });
        bEscape.setOnAction(e -> {
            // Escape cierra el combate y vuelve al mapa (sustituye la acción previa)
            closeCombatAndReturnToMap();
        });

        actionButtons.getChildren().addAll(bBattle, bItem, bDefend, bEscape);

        // Inicializar selección visual
        updateButtonSelection();
    }

    private Button styledButton(String text) {
        Button b = new Button(text);
        b.setMinWidth(140);
        b.setMinHeight(44);
        b.setStyle("-fx-background-color: linear-gradient(#3a7bd5,#00d2ff); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        b.setFont(Font.font(14));
        return b;
    }

    private void installKeyHandlers() {
        root.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            KeyCode code = ev.getCode();

            // Navegación entre botones con flechas
            if (code == KeyCode.LEFT) {
                ev.consume();
                selectedButtonIndex = Math.max(0, selectedButtonIndex - 1);
                updateButtonSelection();
                return;
            }
            if (code == KeyCode.RIGHT) {
                ev.consume();
                selectedButtonIndex = Math.min(buttons.size() - 1, selectedButtonIndex + 1);
                updateButtonSelection();
                return;
            }

            // Enter ejecuta la acción seleccionada
            if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                ev.consume();
                Button sel = buttons.get(selectedButtonIndex);
                if (sel != null) {
                    sel.fire();
                }
                return;
            }

            // Atajos directos 1..4 para botones
            if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
                ev.consume();
                buttons.get(0).fire();
                return;
            }
            if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
                ev.consume();
                if (buttons.size() > 1) {
                    buttons.get(1).fire();
                }
                return;
            }
            if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
                ev.consume();
                if (buttons.size() > 2) {
                    buttons.get(2).fire();
                }
                return;
            }
            if (code == KeyCode.DIGIT4 || code == KeyCode.NUMPAD4) {
                ev.consume();
                if (buttons.size() > 3) {
                    buttons.get(3).fire();
                }
                return;
            }

            // Escape cierra combate
            if (code == KeyCode.ESCAPE) {
                ev.consume();
                closeCombatAndReturnToMap();
            }
        });
    }

    private void updateButtonSelection() {
        for (int i = 0; i < buttons.size(); i++) {
            Button b = buttons.get(i);
            if (i == selectedButtonIndex) {
                b.setStyle("-fx-background-color: linear-gradient(#ffd54f,#ffb300); -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 6; -fx-border-color: #ffffff55; -fx-border-width: 2;");
            } else {
                b.setStyle("-fx-background-color: linear-gradient(#3a7bd5,#00d2ff); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            }
        }
    }

    // Muestra una alerta de información indicando que la acción fue exitosa
    private void showSuccessAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            try {
                if (root.getScene() != null && root.getScene().getWindow() != null) {
                    a.initOwner(root.getScene().getWindow());
                }
            } catch (Throwable ignored) {
            }
            a.showAndWait();
            // devolver foco a la pantalla de combate y mantener selección
            Platform.runLater(root::requestFocus);
        });
    }

    // --- acciones de combate y flujo ---
    // Ejecuta la acción Battle: héroe ataca al primer monstruo vivo, luego los monstruos atacan
    private void onBattle() {

        // Héroe ataca al primer monstruo vivo
        Monster target = null;
        for (Monster m : monsters) {
            if (m.getActualLife() > 0) {
                target = m;
                break;
            }
        }

        if (target == null) {
            // todos muertos
            endCombatAndReturnToMap();
            return;
        }

        boolean heroDidDamage = game.heroCombat(target);
        if (heroDidDamage) {
            // mostrar alerta de acción exitosa
            showSuccessAlert("Battle", "Has atacado a " + target.getName() + ".");
        } else {
            showSuccessAlert("Battle", "Tu ataque no hizo daño.");
        }

        // Si el monstruo murió, removerlo y comprobar si quedan monstruos
        if (game.checkGameOver(target.getActualLife())) {
            removeMonster(target);
            if (monsters.isEmpty()) {
                // victoria: terminar combate y volver al mapa
                endCombatAndReturnToMap();
            }
        }

        // Si aún hay monstruos vivos, que ataquen al héroe
        monstersAttackAfterHeroAction();
    }

    // Después de una acción del héroe (no necesariamente Battle), los monstruos vivos atacan
    private void monstersAttackAfterHeroAction() {
        int cont = 0;
        for (Monster m : new ArrayList<>(monsters)) {
            if (m.getActualLife() <= 0) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Ha atacado");
                a.setHeaderText(null);
                a.setContentText("Ha atacado el monstruo" + cont++);
            }
            boolean monsterDidDamage = game.combat(m);
            // opcional: podrías mostrar mensajes por cada ataque; aquí solo comprobamos muerte
            if (game.checkGameOver(game.getHero().getActualLife())) {
                // héroe muerto -> mostrar Game Over
                showGameOver();
             
                return;
            }
        }
    }

    // Remueve un monstruo de la lista y de la UI
    private void removeMonster(Monster m) {
        int idx = monsters.indexOf(m);
        if (idx >= 0) {
            monsters.remove(idx);
            if (idx < monstersBox.getChildren().size()) {
                final int removeIdx = idx;
                Platform.runLater(() -> monstersBox.getChildren().remove(removeIdx));
            }
        }
    }

    // Termina el combate y vuelve al mapa (usa onExit si está definido)
    private void endCombatAndReturnToMap() {
        Platform.runLater(() -> {
            try {
                FXGL.getGameScene().removeUINode(root);
            } catch (Throwable ignored) {
            }
            if (onExit != null) {
                onExit.run();
            }
        });
    }

    // Cierra combate y regresa al mapa (invocado por Escape o por botón Escape)
    private void closeCombatAndReturnToMap() {
        endCombatAndReturnToMap();
    }

    // Muestra pantalla Game Over con imagen y música; Start lleva al menú principal
    private void showGameOver() {
        Platform.runLater(() -> {
            // Crear overlay si no existe
            if (gameOverOverlay != null) {
                return;
            }

            Pane overlay = new Pane();
            overlay.setPrefSize(800, 600);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");

            Image goImg = null;
            try {
                goImg = new Image(getClass().getResourceAsStream("/Resources/textures/gameOver.png"));
            } catch (Throwable ignored) {
            }
            ImageView goView = new ImageView(goImg);
            goView.setPreserveRatio(true);
            goView.setFitWidth(600);
            goView.setFitHeight(400);
            goView.setLayoutX((800 - 600) / 2.0);
            goView.setLayoutY(60);

            Button startBtn = new Button("Start");
            startBtn.setMinWidth(160);
            startBtn.setMinHeight(44);
            startBtn.setStyle("-fx-background-color: linear-gradient(#ff5f6d,#ffc371); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
            startBtn.setFont(Font.font(16));
            startBtn.setLayoutX((800 - 160) / 2.0);
            startBtn.setLayoutY(500);
            
            Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Falleciste");
                a.setHeaderText(null);
                a.setContentText("Tu heroe fallecio xd");

            startBtn.setOnAction(ev -> {
                // detener música y volver al menú principal
                stopGameOverMusic();
                try {
                    FXGL.getGameScene().removeUINode(root);
                } catch (Throwable ignored) {
                }
                try {
                    FXGL.getGameScene().removeUINode(overlay);
                } catch (Throwable ignored) {
                }
                // Restaurar menú y música
                MainScreen.restoreMenuAndMusic();
            });

            overlay.getChildren().addAll(goView, startBtn);
            gameOverOverlay = overlay;

            // Añadir overlay y reproducir música
            try {
                FXGL.getGameScene().addUINode(overlay);
            } catch (Throwable ignored) {
            }
            playGameOverMusic();
        });
    }

    private void playGameOverMusic() {
        try {
            URL res = getClass().getResource("/Resources/music/gameOver.mp3");
            if (res != null) {
                Media media = new Media(res.toExternalForm());
                gameOverPlayer = new MediaPlayer(media);
                gameOverPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                gameOverPlayer.setVolume(MainScreen.getVolumeSetting());
                gameOverPlayer.play();
            }
        } catch (Throwable ignored) {
        }
    }

    private void stopGameOverMusic() {
        try {
            if (gameOverPlayer != null) {
                gameOverPlayer.stop();
                gameOverPlayer.dispose();
                gameOverPlayer = null;
            }
        } catch (Throwable ignored) {
        }
    }

    public void setOnExit(Runnable onExit) {
        this.onExit = onExit;
    }

    public void show() {
        Platform.runLater(() -> {
            try {
                FXGL.getGameScene().addUINode(root);
            } catch (Throwable ignored) {
            }
            root.requestFocus();
        });
    }
}
