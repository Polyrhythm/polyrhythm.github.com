$(document).ready(function() {
    $('.terminal_window').terminal(function(command, term) {
        if (command == 'about') {
            term.echo("Ryan Jones is a developer based in San Francisco. He'd love to hear from you.");
        } else if (command == 'contact') {
            term.echo('ryan.joshua.jones@gmail.com');
        } else if (command == 'help') {
            term.echo('the commands are listed above, you bozo');
        } else {
            term.echo('unknown command');
        }
    }, { prompt: '> ', name: 'primary', greetings: "[about] [contact]" });

    $('.terminal_window').bind('mousewheel', function(event, delta, deltaX, deltaY) {
        console.log(delta, deltaX, deltaY);
    });
});
