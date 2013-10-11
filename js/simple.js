(function() {
  'use strict';

  var walker,
      path;

  var prevLocation = {
    x: 0,
    y: 0
  };

  var world = new Burner.World(document.body, {
    gravity: new Burner.Vector(0, 1),
    c: 0.25
  });

  Burner.System.init(function() {
    function rN(min, max) {
      return Math.floor(Math.random() * (max - min + 1) + min);
    }

    walker = this.add('Walker', {
      color: [ rN(0, 255), rN(0, 255), rN(0, 255) ],
      maxSpeed: 1
    });
  });

  var callback = function() {
    var newLocation = {
      x: walker.location.x,
      y: walker.location.y
    };

    if (newLocation.x != prevLocation.x && newLocation.y != prevLocation.y) {
      path = document.createElement('div');
      path.className = 'crumb';
      path.style.left = walker.location.x + "px";
      path.style.top = walker.location.y + "px";
      document.body.appendChild(path);
      prevLocation.x = newLocation.x;
      prevLocation.y = newLocation.y;
    }
  };

  setInterval(callback, 250);

})();
