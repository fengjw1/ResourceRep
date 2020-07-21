import Encoder from "./encode/Encoder";
import Decoder from "./encode/Decoder";

//实现参考例子
var message = "减肥是一件困难的事情";

//UTF-8转GBK
var gbkArray = Encoder.hexstrToArray(Encoder.stringToGbkHexstr(message));
console.log("gbkArray>>" + gbkArray);
//GBK转UTF-8
var utfStr = Decoder.GBKHexstrToString(Decoder.binaryarrayToHexstr(gbkArray));
console.log("utfStr>>" + utfStr);