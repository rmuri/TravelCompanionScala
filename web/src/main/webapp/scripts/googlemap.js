function GoogleMap() {

    var map = null;

    this.initialize = function(str) {
        map = new GMap2(document.getElementById(str));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());

        map.setCenter(new GLatLng(47.3666667, 8.55), 3);
    }

    this.setCenter = function(lat,lng) {
         map.setCenter(new GLatLng(lat, lng), 3);
    }

    this.createMarker = function(stage) {
        var point = new GLatLng(stage.lat, stage.lng);
        var marker = new GMarker(point);
        GEvent.addListener(marker, 'click', function() {
            marker.openInfoWindowHtml(stage.name);
        });
        return marker;
    }

    this.createLine = function(marker1, marker2) {
        return new GPolyline([
            new GLatLng(marker1.getLatLng().lat(), marker1.getLatLng().lng()),
            new GLatLng(marker2.getLatLng().lat(), marker2.getLatLng().lng()),
        ], "#000000", 3, 1, {geodesic:true});
    }

    this.addOverlay = function(object) {
        map.addOverlay(object);
    }

    this.createTour = function(markers) {
        var tour = new Tour();

        for (var i = 0; i < markers.length; i++) {
            var tmp1 = markers[i];
            var tmp2 = markers[i + 1];

            tour.addMarker(tmp1);

            if (tmp2 != undefined) {
                tour.addLine(this.createLine(tmp1, tmp2));
            }
        }
        return tour;
    }

    this.drawTour = function(tour) {
        for (var i = 0; i < tour.getMarkers().length; i++) {
            this.addOverlay(tour.getMarkers()[i]);
        }

        for (var i = 0; i < tour.getLines().length; i++) {
            this.addOverlay(tour.getLines()[i]);
        }
    }
}

function Tour() {
    var paths = new Array();
    var points = new Array();

    this.addMarker = function(marker) {
        points.push(marker);
    }

    this.addLine = function(line) {
        paths.push(line);
    }

    this.getLines = function() {
        return paths;
    }

    this.getMarkers = function() {
        return points;
    }

    this.getLength = function() {
        var length = 0;
        for (var i = 0; i < paths.length; i++) {
            length += paths[i].getLength();
        }
        return length;
    }
}

function Stage(n,la,ln) {
    this.name = n;
    this.lat = la;
    this.lng = ln;
}