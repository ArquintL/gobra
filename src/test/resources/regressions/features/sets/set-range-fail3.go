package pkg

func foo() {
  //:: ExpectedOutput(assert_error:assertion_error)
  assert 42 in set[1..10]
}
