from PySide6.QtCore import QLineF, QPointF

def get_self_intersetion(path_points: list[QPointF]) -> QPointF:
    n = len(path_points)
    if n < 3:
        return None
    
    for i in range(n - 1):
        line1_start = path_points[i]
        line1_end = path_points[i + 1]
        line1 = QLineF(line1_start, line1_end)

        for j in range(i + 2, n - 1):
            line2_start = path_points[j]
            line2_end = path_points[j + 1]
            line2 = QLineF(line2_start, line2_end)

            intersection_type, point = line1.intersects(line2)

            if intersection_type == QLineF.IntersectionType.BoundedIntersection:
                return point

    if point_is_on_segment(path_points[-1], path_points[-3], path_points[-2]):
        return path_points[-1]
    else:
        return None

def point_is_on_segment(point: QPointF, seg_start: QPointF, seg_end: QPointF) -> bool:
    line = QLineF(seg_start, seg_end)
    distance_to_start = QLineF(point, seg_start).length()
    distance_to_end = QLineF(point, seg_end).length()
    segment_length = line.length()

    return abs((distance_to_start + distance_to_end) - segment_length) < 1e-6