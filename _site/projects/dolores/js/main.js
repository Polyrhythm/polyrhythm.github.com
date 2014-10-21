(function() {
  'use strict';

  // set up the ~world~
  var addIceCream;
  var normalQuotes = [
    'Man, I would love some ice cream.',
    'Sweet fixie, bro.',
    'Where can I find an overpriced coffee?',
    'Have you heard of #bubblepops?',
    'Let\'s smoke some weed!',
    'Time for some brunch',
    'Gentrification rules!',
    'My dad is paying my rent for me',
    'You work at [insert startup here] too?!',
    'I only buy the finest $20 organic apples.'
  ];
  var fatQuotes = [
    'WHERE\'S THE CORN DOGS?',
    'FEED ME MORE!',
    'MUST EAT MORE THINGS',
    'I CAN\'T RESIST THE DELICIOUS SUGAR!'
  ];

  Flora.System.setup(function() {
    var world = this.add('World', {
      gravity: new Flora.Vector(0, 0),
      c: 0
    });

    var randomLocation = function() {
      var minX = (world.width / 2) * -1;
      var maxX = (world.width / 2);
      var minY = (world.width / 2) * -1;
      var maxY = (world.width / 2);

      return new Flora.Vector(
        (world.width / 2 + Flora.Utils.getRandomNumber(minX, maxX)),
        (world.height / 2 + Flora.Utils.getRandomNumber(minY, maxY))
      );
    };

    var randomVelocity = function() {
      return new Flora.Vector(
        Flora.Utils.getRandomNumber(0, 2, true),
        Flora.Utils.getRandomNumber(0, 2, true)
      );
    };

    var addHipster = function(location, velocity) {
      var walker = this.add('Walker', {
        location: location,
        opacity: 0,
        wrapWorldEdges: true
      });

      var hipster = this.add('Agent', {
        type: 'Hipster',
        color: [0, 255, 0],
        location: location,
        velocity: velocity,
        seekTarget: walker,
        wrapWorldEdges: true,
        sensors: [
          this.add('Sensor', {
            type: 'IceCream',
            targetClass: 'IceCream',
            sensitivity: 300,
            displayRange: false,
            opacity: 0,
            behavior: 'DESTROY',
            onDestroy: function(sensor, iceCream) {
              hipster.width += 2;
              hipster.height += 2;
              if (hipster.maxSpeed > 5) hipster.maxSpeed -= 1;

              if (hipster.width >= 15 && !hipster.firstName.match(/fat/)) {
                hipster.firstName = 'fat-' + hipster.firstName;
              }
            }
          }),
          this.add('Sensor', {
            type: 'IceCream',
            targetClass: 'IceCream',
            sensitivity: 300,
            opacity: 0,
            behavior: 'AGGRESSIVE'
          })
        ],
        beforeStep: function() {
          var translate = function(x, y) {
            return 'translate3d(' + x + 'px, ' +
              y + 'px, 0px)';
          };

          // check for hipster following an ice cream
          for (var i = 0; i < hipster.sensors.length; i++) {
            if (hipster.sensors[i].activated) {
              console.log('OM NOM NOM');
            }
          }

          // built-in global timer for each hipster
          hipster.speechTimer = hipster.speechTimer + 1 || 0;

          // every hipster can 'talk' and has a built-in delay
          // before engaging in another rant about bullshit
          if (!hipster.talking) {
            // hipster has not instantiated a talking timer yet
            hipster.talking = Flora.Utils.getRandomNumber((60 * 3), (60 * 50));
            hipster.speechBubble = false;
          } else if (hipster.talking > 1 && hipster.speechBubble) {
            // hipster has talked recently, update position of the bubble
            hipster.speechBubble.style.transform = translate(
              hipster.location.x,
              hipster.location.y
            );

            // the hipster has had his stupid thought visible to the world
            // long enough
            if (hipster.speechTimer > (60 * 4)) {
              hipster.speechBubble.remove();
              hipster.speechBubble = false;
            }

            hipster.talking -= 1;
          } else if (hipster.talking > 1) {
            // hipster has not said their initial thought yet
            // or has had their thought time out
            hipster.talking -= 1;
          } else {
            // reset the global timer
            hipster.speechTimer = 0;

            // construct a speech bubble
            hipster.speechBubble = document.createElement('span');
            hipster.speechBubble.className = 'speech-container';

            var speechHTML = '<b>' + hipster.firstName + ':<b><br/><br/>';

            // say contextually-correct things!
            var quotes = (hipster.width < 15) ? normalQuotes : fatQuotes;

            speechHTML += quotes[
              Flora.Utils.getRandomNumber(0, quotes.length - 1)
            ];

            hipster.speechBubble.innerHTML = speechHTML;
            hipster.speechBubble.style.transform = translate(
              hipster.location.x,
              hipster.location.y
            );

            document.body.appendChild(hipster.speechBubble);

            // reset delay
            hipster.talking = Flora.Utils.getRandomNumber((60 * 10), (60 * 50));
          }
        }.bind(this)
      });

      hipster.firstName = 'hipster#' + Flora.Utils.getRandomNumber(0, 100);
    }.bind(this);

    // make a handful of hipsters, hungry for some Bi-Rite ice cream
    for (var i = 1; i <= 20; i++) {
      addHipster(randomLocation(), randomVelocity());
    }

    addIceCream = function(x, y) {
      var iceCream = this.add('Agent', {
        type: 'IceCream',
        location: new Flora.Vector(x, y),
        width: 30,
        height: 30,
        isStatic: true,
        color: [255, 255, 255]
      });

      iceCream.type = 'IceCream';
      iceCream.el.classList.add('icecream');
    }.bind(this);
  });

  Flora.System.loop();

  // set up event handlers
  document.body.addEventListener('click', function(e) {
    addIceCream(e.clientX, e.clientY);

    return false;
  });
}());
