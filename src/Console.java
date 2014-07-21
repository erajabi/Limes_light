import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.OutputStream;

class Console extends OutputStream
{
    private TextArea output;

    public Console(TextArea ta)
    {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException
    {
        output.appendText(String.valueOf((char) i));
    }

}