# MovLang
## Currently not in working condition
An esoteric programming language that has only one instruction: copy memory<br>
Command syntax:
```
[<label>:]
<dest> <value>[:<size|bit_size>]
```

Dereferensing syntax:
```
<segment>'['<value>[+<offset>|<bit_offset>]']'
```
'\n' doesn't matter and play the same role as whitespace

Examples:
```d
0[3d5a] _10 :1 // move decimal value 10 with size = 1 byte to address 3d5a in segment '0'
J[0000] 4C :4 // move 76 with size = 4 bytes to address 0000 in segment 'J'
J[1a04+^1] _15 :^4 // move 15 with size = 4 bits to address 1a04 in segment 'J' with bit offset 1
// (now this address is filled with _****___, where _ means old value)
```
