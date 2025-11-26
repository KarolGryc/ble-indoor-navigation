from PySide6.QtCore import QLineF, QPointF

def line_intersects_path(p1: QPointF, p2: QPointF, path_points: list[QPointF]) -> bool:
    if len(path_points) < 2:
        return False

    test_line = QLineF(p1, p2)

    for i in range(len(path_points) - 2):
        path_p1 = path_points[i]
        path_p2 = path_points[i+1]
        
        path_segment = QLineF(path_p1, path_p2)

        intersection_type, _ = test_line.intersects(path_segment)

        if intersection_type == QLineF.IntersectionType.BoundedIntersection:
            return True

    return False