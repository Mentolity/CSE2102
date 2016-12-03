package maps;

import gfx.Map;

public class CherryGroveCityMap5 extends Map{
	public CherryGroveCityMap5(Map parent) {
		super("/House1.png", "/House1PointMap.png");
		doors.add(new Doors(48, 144, parent, 4336, 3552));
		doors.add(new Doors(64, 144, parent, 4336, 3552));
	}
}
