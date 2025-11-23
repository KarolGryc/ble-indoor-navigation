from PySide6.QtGui import QMouseEvent, QPainter
from PySide6.QtWidgets import QGraphicsView, QGraphicsScene
from PySide6.QtCore import Qt
from map_presenter import MapPresenter

class MapView(QGraphicsView):
    def __init__(self, presenter: MapPresenter):
        super().__init__()
        self.presenter = presenter
        self.scene: QGraphicsScene = presenter.scene
        self.setScene(self.scene)
        self.scene.setSceneRect(-50000, -50000, 100000, 100000)

        self.setRenderHint(QPainter.Antialiasing)
        self.setViewportUpdateMode(QGraphicsView.FullViewportUpdate)
        self.setHorizontalScrollBarPolicy(Qt.ScrollBarAlwaysOff)
        self.setVerticalScrollBarPolicy(Qt.ScrollBarAlwaysOff)

        self._is_panning = False
        self._pan_start_pos = None

    def mousePressEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._is_panning = True
            self._pan_start_pos = event.position()
            self.setCursor(Qt.ClosedHandCursor)
            return

        # map the mouse event to graphics scene coordinates
        pos = self.mapToScene(event.position().toPoint())
        self.presenter.on_canvas_click(pos)

        return super().mousePressEvent(event)
    
    def mouseReleaseEvent(self, event: QMouseEvent):
        if event.button() == Qt.MiddleButton:
            self._is_panning = False
            self.setCursor(Qt.ArrowCursor)
            event.accept()
            return

        super().mouseReleaseEvent(event)

    def mouseMoveEvent(self, event: QMouseEvent):
        # --- LOGIKA PRZESUWANIA WIDOKU ---
        if self._is_panning:
            # Obliczamy o ile przesunęła się myszka od ostatniej klatki
            delta = event.position() - self._pan_start_pos
            self._pan_start_pos = event.position()

            # Przesuwamy paski przewijania (scrollbary)
            # Odejmujemy deltę, żeby ruch był naturalny ("ciągniemy papier")
            h_bar = self.horizontalScrollBar()
            v_bar = self.verticalScrollBar()

            print(f"Delta: {delta.x()}, {delta.y()}")  # Debugging line
            
            h_bar.setValue(h_bar.value() - delta.x())
            v_bar.setValue(v_bar.value() - delta.y())
            
            event.accept()
            return

        # --- LOGIKA NARZĘDZI (PREZENTER) ---
        # scene_pos = self.mapToScene(event.pos())
        # self.presenter.on_canvas_move(scene_pos)
        super().mouseMoveEvent(event)