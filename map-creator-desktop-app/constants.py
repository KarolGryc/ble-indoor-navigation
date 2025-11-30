import os
from pathlib import Path

BASE_DIR = Path(__file__).parent.absolute()
ICONS_DIR = BASE_DIR / "icons"

icons = {
    # Tool icons
    "add_wall": os.path.join(ICONS_DIR, "wall_add.png"),
    "add_zone": os.path.join(ICONS_DIR, "zone_add.png"),
    "add_poi": os.path.join(ICONS_DIR, "poi_add.png"),
    "select_move": os.path.join(ICONS_DIR, "select_move.png"),
    "edit_item": os.path.join(ICONS_DIR, "item_edit.png"),
    "connect_zones": os.path.join(ICONS_DIR, "zone_connection.png"),

    # POI icons
    "exit": os.path.join(ICONS_DIR, "exit.png"),
    "generic": os.path.join(ICONS_DIR, "generic.png"),
    "toilet": os.path.join(ICONS_DIR, "toilet.png"),
    "shop": os.path.join(ICONS_DIR, "shop.png"),
    "restaurant": os.path.join(ICONS_DIR, "restaurant.png"),
}

poi_colors = {
    "generic": "#D32C2C",
    "restaurant": "#F57C00",
    "shop": "#FBC02D",
    "toilet": "#1976D2",
    "exit": "#388E3C",
}

SAVE_FILE_EXTENSION = ".json"
DEFAULT_SAVE_FILE_NAME = "building" + SAVE_FILE_EXTENSION

GRID_SIZE_DEFAULT = 25