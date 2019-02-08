# Project structure and Heuristics
* All rules must be upheld for a pull-request to be accepted

## General
* All code must be formatted according to the standard IDEA codestyle, so auto-formatting does not change git annotation
* Use strategy-patterns when possible
    * Other design-patterns are sometimes the better option, if so you should choose one.
* Document all public methods - except getters, setters, constructors without function calls
    * Use correct JavaDoc notation

## GIT
* All commit messages containing code should have both title and description
    * Description should include changes and motivation for all non-trivial changes

## UNit testing
* Use JUnit
* All non-trivial code should be tested
* Document tests when titles are non self-explanatory