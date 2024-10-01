# MovLang
## Currently not in working condition
An esoteric programming language that has only one instruction: copy memory<br>
Command syntax:
```
[<label>:]
[<segment>x]<dest> <value>[:<size|bit_size>]
```

Address syntax:
```
<address>[+<offset>|<bit_offset>]...
```
'\n' doesn't matter and play the same role as whitespace

Examples:
```d
0x3d5a 10 :1 // move decimal value 10 with size = 1 byte to address 3d5a in segment '0'
Jx0000 45 :4 // move 42 with size = 4 bytes to address 0000 in segment 'J'
Jx1a04+^1 15 :^4 // move 15 with size = 4 bits to address 1a04 in segment 'J' with bit offset 1
// (now this address is filled with _****___, _ means old value)
```
