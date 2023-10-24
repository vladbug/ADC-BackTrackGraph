# Call Sequence Generator

This is a project developed for the ADC class in my university. 
Given an extended version of the OpenAPI specification it's possible
to generate call sequences for the API to use them in further steps
of testing purposes.

It's possible to generated nominal and faulty tests with this tool.

## Compile 
In the root directory:
1. `mvn compile assembly:single`
2. the generated fat jar is in the target directory

## Run 
1. `java -jar [path-to-extended-oas] [nr-randoms] [nr-sequences] [threshold]`



# How to use it

## Nominal sequences

In the Main Class the constructor must receive the following information :
- Specification File Operations
- Mode : boolean parameter that will be `true`
- Number of iterations : number of operations in each sequence
- Number of sequences

## Faulty sequences

In the Main Class the constructor must receive the following information :
- Specification File Operations
- Mode : boolean parameter that will be `false`
- Number of iterations : number of operations in each sequence
- Number of sequences
- Threshold : the limit of backtracks allowed in the graph

## TO DO List

- [ ] Create a better approach to deactivate the safety mechanisms in the faulty sequences
- [ ] Re-use certain operations in history with extra logic (optional)
- [ ] Create a filter for repetitive sequences
- [ ] Add a POST limitation filter for some interesting tests
- [ ] Refactor the code