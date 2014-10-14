Physics({
  timestep: 1000.0 / 160,
  maxIPF: 16,
  integrator: 'verlet'
}, function(world) {
  'use strict';

  var viewWidth = 500;
  var viewHeight = 300;

  // add objects
  var ball = Physics.body('circle', {
    x: 50,
    y: 50,
    vx: 0.2,
    vy: 0.01,
    radius: 20
  });
  world.add(ball);

  var slide = Physics.body('convex-polygon', {
    x: 150,
    y: 50,
    vertices: [
      { x: 0, y: 100 },
      { x: 200, y: 0 },
      { x: 0, y: 0 }
    ],
    restitution: 0.2
  });
  world.add(slide);

  var ramp = Physics.body('convex-polygon', {
    x: 250,
    y: 50,
    vertices: [
      { x: 0, y: 0 },
      { x: 50, y: 50 },
      { x: 50, y: 0 }
    ],
    restitution: 0.2
  });
  world.add(ramp);

  // add some shit
  var viewportBounds = Physics.aabb(0, 0, viewWidth, viewHeight);
  world.add(Physics.behavior('constant-acceleration'));
  world.add(Physics.behavior('body-impulse-response'));
  world.add(Physics.behavior('edge-collision-detection', {
    aabb: viewportBounds,
    restitution: 0.5,
    cof: 0.9
  }));
  world.add(Physics.behavior('body-collision-detection'));
  world.add(Physics.behavior('sweep-prune'));

  // add renderer
  var renderer = Physics.renderer('canvas', {
    el: 'app',
    width: viewWidth,
    height: viewHeight,
    meta: true,
    styles: {
      'circle' : {
        strokeStyle: '#351024',
        lineWidth: 1,
        fillStyle: '#d33682',
        angleIndicator: '#351024'
      }
    }
  });
  world.add(renderer);

  // add a step function
  world.on('step', function() {
    world.render();
  });

  // start the sim
  Physics.util.ticker.on(function(time, dt) {
    world.step(time);
  });

  Physics.util.ticker.start();
});
