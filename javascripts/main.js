$(document).ready(function() {
    var greeting = "thank you for visiting my terminal. please enjoy your stay.\n\n" +
        "[about] [contact]" +
        " | type 'help' for additional commands.\n\n";

    $('.terminal_window').terminal(function(command, term) {
        switch(command.toLowerCase()) {
        case "help":
            term.echo("more soon");
            break;

        case "about":
            term.echo("ryan jones is a developer in san francisco. he'd love to hear from you\n" +
                     "http://github.com/polyrhythm");
            break;

        case "contact":
            term.echo("ryan.joshua.jones@gmail.com");
            break;

        default:
            term.echo(command + " is not a recognized command");
            break;
        }
    }, { prompt: '> ', name: 'primary', greetings: greeting, clear: false });

    $('.terminal_window').bind('mousewheel', function(event, delta, deltaX, deltaY) {
        console.log(delta, deltaX, deltaY);
    });
});
