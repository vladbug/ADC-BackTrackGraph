# Call Sequence Generator

This is a project developed for the ADC class in my university. 
Given an extended version of the OpenAPI specification it's possible
to generate call sequences for the API to use them in further steps
of testing purposes.

It's possible to generated nominal and faulty tests with this tool.

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