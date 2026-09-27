# HTTP Codes per Method
- Register User
  - 

# Colour Codes
- <span style="color: red;">Red</span> = Error
- <span style="color: lightgreen;">Green</span> = Success
- <span style="color: yellow;">Yellow</span> = Warning
- <span style="color: grey;">Grey</span> = Default option, subtle prompt, error details
- <span style="color: cyan;">Cyan</span> = Branding
- <span style="color: blue;">Blue</span> = Sub-Branding
- <span style="color: purple;">Purple</span> = Prompt

# Other Info
- Errors should only be caught by the method calling them in the `App` class
  - Here, they should always be displayed with `cosOuts.displayError(e.getMessage())`