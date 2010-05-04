function Gauge() {
	var _self = this;
	var objImg = new Image();
	var canvas;
	var phi;

	
	this.initialize = function(value,canvasid) {
		canvas = canvasid;
		calcRotation(value);
	
		objImg.src = "/classpath/gauge/gauge.png";
		objImg.onload  	= initCanvas;
		
	}
	
	var initCanvas = function() {
		var objCanvas = document.getElementById(canvas);
	
		if(objCanvas != null) {
			if(objCanvas.getContext){
				draw(objCanvas);
			}
		}
	}
	
	var calcRotation = function(value) {
		///todo
		phi = value*Math.PI/50;
		if(phi < 6) { phi += 0.5; }
		if(phi > 6) { phi = 6; }
	}
	
	
	var draw = function(objCanvas){
	  
	  // Kontext-Objekt
	  var objContext = objCanvas.getContext("2d");
	
	  objContext.clearRect(0, 0, 134, 134);  // Anzeigebereich leeren
	  objContext.drawImage(objImg, 0, 0);    // Ziffernblatt zeichnen
	
	  objContext.save();                     // Ausgangszustand speichern
	  objContext.translate(67, 67);          // Koordinatensystem in Mittelpkt des Ziffernblatts verschieben
	
	  objContext.save();

	  objContext.rotate(phi);
	  
	  objContext.beginPath();                // Neuen Pfad anlegen
	  objContext.moveTo(0, 5);              // Zeiger Ueber Mitte hinaus zeichnen
	  objContext.lineTo(0, 60);             // im Koord-Sys. um 60 Einheiten nach unten zeichnen
	  // Linienstyle festlegen und zeichnen
	  objContext.lineWidth = 3;
	  objContext.strokeStyle = "#282828";
	  objContext.stroke();
	  objContext.restore();
		
	  objContext.restore();
	
	
	}
}