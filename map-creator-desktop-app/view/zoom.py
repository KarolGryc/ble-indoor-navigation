from PySide6.QtWidgets import QGraphicsView

class GraphicsViewZoom:
    def __init__(self, 
                 min_zoom: float = 0.2, 
                 max_zoom: float = 10.0, 
                 scale: float = 1.1):
        self._zoom_factor = 1.0
        self._scale = scale
        self._min_zoom = min_zoom
        self._max_zoom = max_zoom

    def zoom_in(self, view: QGraphicsView):
        new_zoom = self._zoom_factor * self._scale
        if new_zoom > self._max_zoom:
            return

        self._zoom_factor = new_zoom
        view.scale(self._scale, self._scale)

    def zoom_out(self, view: QGraphicsView):
        new_zoom = self._zoom_factor / self._scale
        if new_zoom < self._min_zoom:
            return

        self._zoom_factor = new_zoom
        view.scale(1 / self._scale, 1 / self._scale)

    def reset_zoom(self, view: QGraphicsView):
        view.resetTransform()
        self._zoom_factor = 1.0