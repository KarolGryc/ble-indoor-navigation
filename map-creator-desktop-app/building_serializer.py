import json
import uuid

from PySide6.QtCore import QPointF
from model import (
    Building, Floor, Node, Zone, PointOfInterest, 
    Wall, ZoneType, PointOfInterestType
)

class BuildingSerializer:
    def save_to_file(self, building: Building, file_path: str):
        with open(file_path, 'w', encoding='utf-8') as f:
            data = self.serialize(building)
            json.dump(data, f, indent=4)
            return True
        
        return False

    def load_from_file(self, file_path: str) -> Building:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            return self.deserialize(data)

    def serialize(self, building: Building) -> dict:
        return building.to_dict()
    
    def deserialize(self, data: dict) -> Building:
        building = Building()
        id_map = {}

        for floor_data in data.get("floors"):
            floor_name = floor_data.get("name", "Unnamed Floor")
            floor_uuid = uuid.UUID(floor_data.get("id"))
            floor = Floor(floor_name, floor_uuid)
            
            id_map[floor_uuid] = floor
            building.add_floor(floor)

        for node_data in floor_data.get("nodes", []):
            pos_x = node_data.get("x")
            pos_y = node_data.get("y")
            node_uuid = uuid.UUID(node_data.get("id"))
            node = Node(pos_x, pos_y, None, node_uuid)

            floor.add(node)
            id_map[node_uuid] = node

        for zone_data in floor_data.get("zones", []):
            zone_name = zone_data.get("name", "Unnamed Zone")
            zone_type = ZoneType[zone_data.get("type")]
            zone_uuid = uuid.UUID(zone_data.get("id"))

            corner_node_ids = zone_data.get("corner_node_ids", [])
            corner_nodes = [id_map[uuid.UUID(node_id)] for node_id in corner_node_ids]

            zone = Zone(corner_nodes, zone_name, zone_type, zone_uuid)

            floor.add(zone)
            id_map[zone_uuid] = zone

        for poi_data in floor_data.get("points_of_interest", []):
            poi_name = poi_data.get("name", "Unnamed POI")
            pos_x = float(poi_data.get("x"))
            pos_y = float(poi_data.get("y"))
            poi_uuid = uuid.UUID(poi_data.get("id"))
            poi_type = PointOfInterestType[poi_data.get("type").upper()]

            poi = PointOfInterest(QPointF(pos_x, pos_y), poi_name, poi_type, poi_uuid)

            floor.add(poi)
            id_map[poi_uuid] = poi

        for wall_data in floor_data.get("walls", []):
            start_node_id = uuid.UUID(wall_data.get("start_node_id"))
            end_node_id = uuid.UUID(wall_data.get("end_node_id"))
            wall_uuid = uuid.UUID(wall_data.get("id"))

            start_node = id_map[start_node_id]
            end_node = id_map[end_node_id]

            wall = Wall(start_node, end_node, wall_uuid)

            floor.add(wall)
            id_map[wall_uuid] = wall

        for connection_data in floor_data.get("zone_connections", []):
            zone1_id = uuid.UUID(connection_data.get("zone1_id"))
            zone2_id = uuid.UUID(connection_data.get("zone2_id"))

            zone1 = id_map[zone1_id]
            zone2 = id_map[zone2_id]

            building.add_zone_connection(zone1, zone2)

        return building    