$(document).ready(function() {
    $('.terminal_window').terminal(function(command, term) {
        switch(command.toLowerCase()) {
        case "help":
            term.echo("if this command is necessary then i have failed");
            break;

        case "about":
            term.echo("ryan jones is a developer in san francisco. he'd love to hear from you");
            break;

        case "contact":
            term.echo("ryan.joshua.jones@gmail.com");
            break;

        default:
            term.echo(command + " is not a recognized command");
            break;
        }
    }, { prompt: '> ', name: 'primary', greetings: "[about] [contact]" });

    $('.terminal_window').bind('mousewheel', function(event, delta, deltaX, deltaY) {
        console.log(delta, deltaX, deltaY);
    });
});
