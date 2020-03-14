NetAddr.localAddr;

(
// other layer begins silence
~beginFunc = OSCFunc({ arg msg, time, addr, recvPort; [msg, time, addr, recvPort].postln; }, '/begin-iab'); // create the OSCFunc
// other layer ends silence
~endFunc = OSCFunc({ arg msg, time, addr, recvPort; [msg, time, addr, recvPort].postln; }, '/end-iab'); // create the OSCFunc
)

~beginFunc.free;
~endFunc.free;