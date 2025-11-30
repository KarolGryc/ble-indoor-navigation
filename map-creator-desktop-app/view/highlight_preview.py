import shiboken6

class HighlightPreview:
    def __init__(self):
        self._highlight_item = None

    def update_preview(self, item):
        self.clear()

        # Ensure item still exists
        if item is not None and shiboken6.isValid(item):
            self._highlight_item = item
            self._highlight_item.set_highlight(True)

    def clear(self):
        # Ensure item still exists
        if self._highlight_item and shiboken6.isValid(self._highlight_item):
            self._highlight_item.set_highlight(False)
            self._highlight_item = None