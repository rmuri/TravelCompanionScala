function Gauge() {
	var _self = this;
	var canvas;
	var initvalue;
	var finished = false;


	this.initialize = function(value,canvasid) {
		canvas = canvasid;
		initvalue = value;
		objImg.onload  	= initCanvas;
	}

	var initCanvas = function() {
		var objCanvas = document.getElementById(canvas);

		if(objCanvas != null) {
			if(objCanvas.getContext){
				draw(objCanvas,0);
			}
		}
	}

	var calcRotation = function(value) {
		///todo
		var phi = (value/1.51)*Math.PI/50+(0.57);
		if(phi < 6) { phi += 0.5; }
		if(phi > 6) { phi = 6; }

		return phi;
	}


	var draw = function(objCanvas,i){

	  var phi = calcRotation(i);

	  // Kontext-Objekt
	  var objContext = objCanvas.getContext("2d");

	  objContext.clearRect(0, 0, 269, 269);  // Anzeigebereich leeren
	  objContext.drawImage(objImg, 0, 0);    // Ziffernblatt zeichnen

	  objContext.font = "20pt Arial";
	  objContext.textAlign = 'center';
	  objContext.textBaseline = 'middle';

	  objContext.fillText(i+"%", 145, 220);



	  objContext.save();                     // Ausgangszustand speichern
	  objContext.translate(135, 135);          // Koordinatensystem in Mittelpkt des Ziffernblatts verschieben

	  objContext.save();

	  objContext.rotate(phi);
	              // Zeiger Ì?ber Mitte hinaus zeichnen
	  objContext.drawImage(objImg2, -10, -35);

	  objContext.restore();

	  objContext.restore();

	  if(i<initvalue) {
		window.setTimeout(function() { draw(objCanvas,++i); },40);
	  } else {
			//alert(phi);
	  }


	}
}