# Software Rasterizer
This project is for educational purposes and aims to explain modern graphics pipelines (such as OpenGL), from the inner-workings of a GPU to the most front-end graphics API calls.

# Introduction
In this project I will implement a 3D graphics rasterizer that will contain the major features of modern graphics pipelines.
I will cover loading simple 3D model formats such as Wavefront OBJ, representing the loaded information as stored on modern computer systems, then passing this data to the rasterizer engine that will project 3D points into a 2D image.

## Supported features
+ OBJ file loading
+ Calculated face normals using verticies position and direction
+ Per-pixel world coordinates
+ Pixel shaders
+ Light support
+ Built-in shaders
+ Znear and zfar clipping
+ Camera FOV
+ Void colour
+ Back/Front face culling
+ Frustum culling
+ GLBlendEquation equivalent 
+ Texture mapping
+ Single color textures
+ Texture transparrency
+ Texture wrapping
+ Texture offset
+ Camera textures
+ Depth buffer

## Limitations
+ Texture dimensions must be power of two
+ Model faces must have only three verticies

## Future features
+ Vertex colours
+ MTL loading
+ Split non-triangular faces to triangles
+ Rendering off screen
+ Rendering modes like GLBegin
+ Matrix stacking