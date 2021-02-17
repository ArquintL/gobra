// Any copyright is dedicated to the Public Domain.
// http://creativecommons.org/publicdomain/zero/1.0/

package main

func test() {
	s0 := "test0"
	s1 := `test1`
	/* multi line raw strings are currently not supported by the preprocessor
	s2 := `\n
	\n`
	*/
	s3 := "\""
	s4 := "Hello, world!\n"
}

func lenTest() {
  h1 := "hello"
  h2 := "world"
  assert len(h1) == 5
  assert len(h2) == 5
  assert len(h1 + h2) == 10
}

func lenTestFail() {
  h1 := "hello"
  assert len(h1) == 4
}

func stringEq(){
  h1 := "hello"
  var h2 = "hello"
  assert h1 == h2
}