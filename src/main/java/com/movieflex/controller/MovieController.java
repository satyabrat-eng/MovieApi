package com.movieflex.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieflex.dto.MovieDto;
import com.movieflex.dto.MoviePageResponse;
import com.movieflex.entities.Movie;
import com.movieflex.exceptions.EmptyFileException;
import com.movieflex.service.MovieService;
import com.movieflex.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;


    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovirHandler(@RequestPart MultipartFile file, @RequestPart
                                                    String movieDto) throws IOException, EmptyFileException {

        if(file.isEmpty()){
            throw new EmptyFileException("File is Empty! please send another file");
        }
    MovieDto dto= convertToMovieDto(movieDto);
        return new ResponseEntity<>(movieService.addMovie(dto,file), HttpStatus.CREATED);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(@PathVariable Integer movieId){
        return ResponseEntity.ok(movieService.getMovie(movieId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }
    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(@PathVariable Integer movieId,
                                                       @RequestPart(required = false) MultipartFile file,
                                                       @RequestPart String movieDtoObj) throws IOException {
        if (file == null || file.isEmpty())
            file = null;
        MovieDto movieDto = convertToMovieDto(movieDtoObj);
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieDto, file));
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<String> deleteMovieHandler(@PathVariable Integer movieId) throws IOException {
        return ResponseEntity.ok(movieService.deleteMovie(movieId));
    }
    private MovieDto convertToMovieDto(String movieDtoObj) throws JsonProcessingException {
        MovieDto movieDto=new MovieDto();
        ObjectMapper objectMapper=new ObjectMapper();
        movieDto=objectMapper.readValue(movieDtoObj,MovieDto.class);
        return movieDto;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/allMoviesPage")
    public ResponseEntity<MoviePageResponse> getmovieWithPagination(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                    @RequestParam(defaultValue = AppConstants.PAGE_SIZE,required = false)Integer pageSize){
        return ResponseEntity.ok(movieService.getAllMoviesWithPagination(pageNumber,pageSize));

    }

    @GetMapping("/allMoviesPageSort")
    public ResponseEntity<MoviePageResponse> getmovieWithPaginationAndSorting(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
                                                                              @RequestParam(defaultValue = AppConstants.PAGE_SIZE,required = false)Integer pageSize,
                                                                              @RequestParam(defaultValue = AppConstants.SORT_BY,required = false)String sortBy,
                                                                              @RequestParam(defaultValue = AppConstants.SORT_DIR,required = false)String dir
                                                                              ){
        return ResponseEntity.ok(movieService.getAllMoviesWithPaginationAndShorting(pageNumber,pageSize,sortBy,dir));

    }
}
