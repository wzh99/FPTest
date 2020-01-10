package cn.edu.sjtu.ddst.fptest;

public enum ModifierScope {
    NONE, // none of the methods use strictfp
    METHOD, // randomly apply strictfp on each method or not
    CLASS // use strictfp on the whole class (all methods)
}